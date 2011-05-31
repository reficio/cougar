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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reficio.stomp.*;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.FramePreprocessor;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.core.ValidatingPreprocessor;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.domain.Header;
import org.reficio.stomp.domain.HeaderType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ClientImpl implements Client {

	private static final Log logger = LogFactory.getLog(ClientImpl.class);

    private String hostname;
    private int port;
    private String username;
    private String password;
    private String encoding;

    protected StompWireFormat wireFormat;
    protected FramePreprocessor preprocessor;

	private String sessionId;	

    protected Socket socket;
    protected Writer writer;
    protected Reader reader;

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int DEFAULT_TIMEOUT = 600;




    private AtomicBoolean operational;
    private ResourceState state;

	// ----------------------------------------------------------------------------------
	// StompResource methods
	// ----------------------------------------------------------------------------------
	public ClientImpl() {
        this.state = ResourceState.NEW;
        this.operational = new AtomicBoolean(false);
        // TODO delegate creation to factory methods???
        this.wireFormat = new WireFormatImpl();
        this.preprocessor = new ValidatingPreprocessor();
	}
	
	// ----------------------------------------------------------------------------------
	// Helper connection state modifiers
	// ----------------------------------------------------------------------------------
	protected void connect() {
		Frame frame = new Frame(CommandType.CONNECT);
		frame.login(username);
		frame.passcode(password);
		marshall(frame);

		Frame handshake = unmarshall();
        if(handshake.getCommand().equals(CommandType.CONNECTED) == false) {
            this.setState(ResourceState.ERROR);
            throw new StompProtocolException("Expected CONNECTED command, instead received "
                    + handshake.getCommand().name());
        }
        Header session = handshake.getHeader(HeaderType.SESSION);
        if(session != null) {
            setSessionId(session.getValue());
        } else {
            logger.warn("Server does not returned session id");
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
    protected void doSetAttributes(String hostname, int port, String username, String password, String encoding) {
        this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
        this.encoding = encoding != null ? encoding : DEFAULT_ENCODING;
    }

    private void doInitialize(int timeout) {
        logger.info(String.format("Initializing connection=[%s]", this));
        initializeCommunication(timeout);
        setState(ResourceState.COMMUNICATION_INITIALIZED);
		connect();
        setState(ResourceState.OPERATIONAL);
    }

	@Override
	public synchronized void init(String hostname, int port, String username, String password, String encoding) {
		assertNew();
        doSetAttributes(hostname, port, username, password, encoding);
        doInitialize(DEFAULT_TIMEOUT);
	}

	@Override
	public synchronized void init(String hostname, int port, String username, String password, String encoding, int timeout) {
		assertNew();
        doSetAttributes(hostname, port, username, password, encoding);
        doInitialize(timeout);
    }
	
	@Override
	public synchronized void close() {
		assertOperational();
		logger.info(String.format("Closing connection=[%s]", this));

        setState(ResourceState.CLOSING);
		disconnect();
        closeCommunication();
        setState(ResourceState.CLOSED);
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public String getUsername() {
		return username;
	}

    @Override
    public String getEncoding() {
        return encoding;
    }

    protected synchronized ResourceState getState() {
        return this.state;
    }

    protected synchronized void setState(ResourceState state) {
        this.state = state;
        if(state.equals(ResourceState.OPERATIONAL)) {
            setOperational(true);
        } else {
            setOperational(false);
        }
    }

    private void setOperational(boolean value) {
        this.operational.set(value);
    }

    // ----------------------------------------------------------------------------------
	// StompAccessor methods
	// ----------------------------------------------------------------------------------
	protected Frame unmarshall() throws StompException {
		logger.info("Receiving frame: ");
        try {
		    Frame frame = wireFormat.unmarshal(reader);
            logger.info(frame);
		    return frame;
        } catch(RuntimeException ex) {
            setState(ResourceState.ERROR);
            throw ex;
        }
	}

	protected void marshall(Frame frame) throws StompException {
		logger.info("Sending frame: \n" + frame);
        try {
		    wireFormat.marshal(frame, writer);
        } catch(RuntimeException ex) {
            setState(ResourceState.ERROR);
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
        return this.operational.get();
    }

	// ----------------------------------------------------------------------------------
	// Helper methods - connection state verification
	// ----------------------------------------------------------------------------------
    protected void assertOperational() {
        if(isInitialized() == false) {
            ResourceState state = getState();
            throw new StompConnectionException(String.format("Connection is not operational. Connection state is [%s]", state));
        }
    }

     protected void assertNew() {
        ResourceState state = getState();
        if(state.equals(ResourceState.NEW) == false) {
            throw new StompConnectionException("Connection is not in NEW state");
        }
    }


	// ----------------------------------------------------------------------------------
	// Private getter and setters -> helpers
	// ----------------------------------------------------------------------------------
    protected void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }




    // ----------------------------------------------------------------------------------
	// Socket handlers
	// ----------------------------------------------------------------------------------
    protected void initializeCommunication(int timeout) {
        initializeSocket(timeout);
        initializeStreams(timeout);
    }

    protected void closeCommunication() {
        closeStreams();
        closeSocket();
    }

    protected void initializeSocket(int timeout) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), timeout);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding));
        } catch (IOException e) {
            setState(ResourceState.ERROR);
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    protected void initializeStreams(int timeout) {
       try {
           reader = new InputStreamReader(socket.getInputStream(), encoding);
           writer = new OutputStreamWriter(socket.getOutputStream(), encoding);
       } catch (UnsupportedEncodingException e) {
           setState(ResourceState.ERROR);
           throw new StompEncodingException("Error during connection initialization", e);
       } catch (IOException e) {
           setState(ResourceState.ERROR);
           throw new StompConnectionException("Error during connection initialization", e);
       }
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
            reader.close();
        } catch (IOException e) {
            // Ignore that
        }
        try {
            writer.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

}
