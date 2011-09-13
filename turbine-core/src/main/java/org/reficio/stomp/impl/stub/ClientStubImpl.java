package org.reficio.stomp.impl.stub;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.StompException;
import org.reficio.stomp.StompProtocolException;
import org.reficio.stomp.core.*;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.domain.Header;
import org.reficio.stomp.domain.HeaderType;
import org.reficio.stomp.impl.WireFormatImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.reficio.stomp.core.StompResourceState.*;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 13/09/11
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientStubImpl<T extends StompResource> implements StompAccessor {

private static final transient Logger log = LoggerFactory.getLogger(ClientStubImpl.class);

    // ----------------------------------------------------------------------------------
    // required properties pre-initialized with default values
    // ----------------------------------------------------------------------------------
    private String hostname = DEFAULT_HOSTNAME;
    private int port = DEFAULT_PORT;
    private String encoding = DEFAULT_ENCODING;
    private int timeout = DEFAULT_TIMEOUT_IN_MILLIS;
    private String username;
    private String password;

    protected StompWireFormat wireFormat;
    protected FramePreprocessor preprocessor;

    private String sessionId;
    protected Socket socket;
    protected Writer writer;
    protected Reader reader;

    private Boolean operational;
    private StompResourceState state;

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 61613;
    public static final int DEFAULT_TIMEOUT_IN_MILLIS = 1000;


    // ----------------------------------------------------------------------------------
    // StompResource methods
    // ----------------------------------------------------------------------------------
    protected ClientStubImpl() {
        setState(NEW);
        this.wireFormat = createWireFormat();
        this.preprocessor = createFramePreprocessor();
    }

    // ----------------------------------------------------------------------------------
    // Factory methods - dependencies injection - may be overridden by subclasses
    // ----------------------------------------------------------------------------------
    protected StompWireFormat createWireFormat() {
        return new WireFormatImpl();
    }

    protected FramePreprocessor createFramePreprocessor() {
        return new FrameValidator();
    }

    // ----------------------------------------------------------------------------------
    // Helper connection state modifiers
    // ----------------------------------------------------------------------------------
    protected void connect() {
        Frame frame = new Frame(CommandType.CONNECT);
        frame.login(username);
        frame.passcode(password);
        // TODO verify
        frame.encoding(encoding);
        marshall(frame);

        Frame handshake = unmarshall();
        if (handshake.getCommand().equals(CommandType.CONNECTED) == false) {
            closeCommunicationOnError();
            throw new StompProtocolException("Expected CONNECTED command, instead received "
                    + handshake.getCommand().name());
        }
        Header session = handshake.getHeader(HeaderType.SESSION);
        if (session != null) {
            setSessionId(session.getValue());
        } else {
            log.warn("Server has not returned session id");
        }
    }

    protected void disconnect() {
        Frame frame = new Frame(CommandType.DISCONNECT);
        frame.session(sessionId);
        marshall(frame);
    }

    // ----------------------------------------------------------------------------------
    // StompResource methods
    // ----------------------------------------------------------------------------------
    public void init() {
        // due to default values validation of parameters is not required
        log.info(String.format("Initializing connection=[%s]", this));
        assertNew();
        initializeCommunication(timeout);
        setState(COMMUNICATION_INITIALIZED);
        connect();
        setState(OPERATIONAL);
    }

    public void close() {
        assertOperational();
        log.info(String.format("Closing connection=[%s]", this));
        setState(CLOSING);
        disconnect();
        closeCommunication();
        setState(CLOSED);
    }

    // ----------------------------------------------------------------------------------
    // StompAccessor methods
    // ----------------------------------------------------------------------------------
    protected Frame unmarshall() throws StompException {
        if (log.isInfoEnabled())
            log.info("Receiving frame: ");
        try {
            Frame frame = wireFormat.unmarshal(reader);
            if (log.isInfoEnabled())
                log.info(frame.toString());
            return frame;
        } catch (RuntimeException ex) {
            closeCommunicationOnError();
            throw ex;
        }
    }

    protected void marshall(Frame frame) throws StompException {
        if (log.isInfoEnabled())
            log.info("Sending frame: \n" + frame);
        try {
            wireFormat.marshal(frame, writer);
        } catch (RuntimeException ex) {
            closeCommunicationOnError();
            throw ex;
        }
    }

    @Override
    public Frame receive() throws StompException {
        assertOperational();
        return unmarshall();
    }

    @Override
    public void send(Frame frame) throws StompException {
        assertOperational();
        marshall(checkNotNull(frame));
    }

    public boolean isInitialized() {
        return this.operational;
    }

    // ----------------------------------------------------------------------------------
    // Helper methods - connection state verification
    // ----------------------------------------------------------------------------------
    protected void assertOperational() {
        if (isInitialized() == false) {
            throw new StompConnectionException(String.format("Connection is not operational. Connection state is [%s]", getState()));
        }
    }

    protected void assertNew() {
        if (getState().equals(NEW) == false) {
            throw new StompConnectionException("Connection is not in NEW state");
        }
    }

    // ----------------------------------------------------------------------------------
    // Communication and socket handlers
    // ----------------------------------------------------------------------------------
    protected void initializeCommunication(int timeout) {
        initializeSocket(timeout);
        initializeStreams(timeout);
    }

    protected void initializeSocket(int timeout) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), timeout);
        } catch (IOException e) {
            closeCommunicationOnError();
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    protected void initializeStreams(int timeout) {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding));
        } catch (UnsupportedEncodingException e) {
            closeCommunicationOnError();
            throw new StompEncodingException("Error during connection initialization", e);
        } catch (IOException e) {
            closeCommunicationOnError();
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    protected void closeCommunication() {
        closeStreams();
        closeSocket();
    }

    protected void closeCommunicationOnError() {
        setState(ERROR);
        closeCommunication();
    }

    protected void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

    protected void closeStreams() {
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            // Ignore that
        }
        try {
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

    // ----------------------------------------------------------------------------------
    // Connection state mutators
    // ----------------------------------------------------------------------------------
    protected StompResourceState getState() {
        return this.state;
    }

    protected void setState(StompResourceState state) {
        this.state = state;
        setOperational(state.equals(OPERATIONAL));
    }

    private void setOperational(boolean value) {
        this.operational = value;
    }

    // ----------------------------------------------------------------------------------
    // Factory methods - only way to instantiate this class - parameters VALIDATED
    // ----------------------------------------------------------------------------------
    public T hostname(String hostname) {
        assertNew();
        setHostname(hostname);
        return (T)this;
    }

    public T port(int port) {
        assertNew();
        setPort(port);
        return (T)this;
    }

    public T username(String username) {
        assertNew();
        setUsername(username);
        return (T)this;
    }

    public T password(String password) {
        assertNew();
        setPassword(password);
        return (T)this;
    }

    public T encoding(String encoding) {
        assertNew();
        setEncoding(encoding);
        return (T)this;
    }

    public T timeout(int timeout) {
        assertNew();
        setTimeout(timeout);
        return (T)this;
    }

    // ----------------------------------------------------------------------------------
    // Options getters and setters - parameters NOT validated (setters for internal usage)
    // ----------------------------------------------------------------------------------
    protected void setHostname(String hostname) {
        this.hostname = checkNotNull(hostname, "hostname cannot be null");
    }

    public String getHostname() {
        return hostname;
    }

    protected void setPassword(String password) {
        this.password = checkNotNull(password, "password cannot be null");
    }


    public String getPassword() {
        return password;
    }

    protected void setPort(int port) {
        checkArgument(port > 0, "port must be positive");
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    protected void setTimeout(int timeout) {
        checkArgument(timeout >= 0, "timeout must be positive or zero");
        this.timeout = timeout;
    }


    public int getTimeout() {
        return timeout;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = checkNotNull(sessionId, "sessionId cannot be null");
    }


    public String getSessionId() {
        return sessionId;
    }

    protected void setUsername(String username) {
        this.username = checkNotNull(username, "username cannot be null");
    }


    public String getUsername() {
        return username;
    }

    protected void setEncoding(String encoding) {
        this.encoding = checkNotNull(encoding, "encoding cannot be null");
    }


    public String getEncoding() {
        return encoding;
    }

}
