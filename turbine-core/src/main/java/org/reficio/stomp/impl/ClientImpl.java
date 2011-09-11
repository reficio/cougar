/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.reficio.stomp.impl;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.StompException;
import org.reficio.stomp.StompProtocolException;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.FramePreprocessor;
import org.reficio.stomp.core.FrameValidator;
import org.reficio.stomp.core.StompResourceState;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.domain.Header;
import org.reficio.stomp.domain.HeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.reficio.stomp.core.StompResourceState.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ClientImpl implements Client {

    private static final transient Logger log = LoggerFactory.getLogger(ClientImpl.class);

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
    protected ClientImpl() {
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
    @Override
    public void init() {
        // due to default values validation of parameters is not required
        log.info(String.format("Initializing connection=[%s]", this));
        assertNew();
        initializeCommunication(timeout);
        setState(COMMUNICATION_INITIALIZED);
        connect();
        setState(OPERATIONAL);
    }

    @Override
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
        marshall(frame);
    }

    @Override
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
    public static ClientImpl create() {
        return new ClientImpl();
    }

    @Override
    public ClientImpl hostname(@NotNull String hostname) {
        assertNew();
        setHostname(hostname);
        return this;
    }

    @Override
    public ClientImpl port(int port) {
        assertNew();
        setPort(port);
        return this;
    }

    @Override
    public ClientImpl username(@NotBlank String username) {
        assertNew();
        setUsername(username);
        return this;
    }

    @Override
    public ClientImpl password(@NotBlank String password) {
        assertNew();
        setPassword(password);
        return this;
    }

    @Override
    public ClientImpl encoding(@NotBlank String encoding) {
        assertNew();
        setEncoding(encoding);
        return this;
    }

    @Override
    public ClientImpl timeout(int timeout) {
        assertNew();
        setTimeout(timeout);
        return this;
    }

    // ----------------------------------------------------------------------------------
    // Options getters and setters - parameters NOT validated (setters for internal usage)
    // ----------------------------------------------------------------------------------
    protected void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    protected void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassword() {
        return password;
    }

    protected void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    protected void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    protected void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

}
