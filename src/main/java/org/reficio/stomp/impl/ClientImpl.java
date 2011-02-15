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
import org.reficio.stomp.EncodingException;
import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompException;
import org.reficio.stomp.StompProtocolException;
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
	private boolean initialized;
    private boolean error;
    protected boolean operational;

    protected Socket socket;
    protected Writer writer;
    protected Reader reader;

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int DEFAULT_TIMEOUT = 600;

	// ----------------------------------------------------------------------------------
	// StompResource methods
	// ----------------------------------------------------------------------------------
	public ClientImpl() {
	}
	
	// ----------------------------------------------------------------------------------
	// Helper connection state modifiers
	// ----------------------------------------------------------------------------------
	private void connect() {
		Frame frame = new Frame(CommandType.CONNECT);
		frame.login(username);
		frame.passcode(password);
		send(frame);

		Frame handshake = receive();
        if(handshake.getCommand().equals(CommandType.CONNECTED) == false) {
            this.setError(true);
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
		send(frame);
	}
	
	// ----------------------------------------------------------------------------------
	// StompResource methods
	// ----------------------------------------------------------------------------------
    protected void doSetAttributes(String hostname, int port, String username, String password, String encoding) {
        this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.initialized = false;
        this.error = false;
        this.operational = false;
        this.encoding = encoding != null ? encoding : DEFAULT_ENCODING;

        // TODO delegate creation to factory methods???
        this.wireFormat = new WireFormatImpl();
        this.preprocessor = new ValidatingPreprocessor();
    }

    private void doInitialize(int timeout) {
        logger.info(String.format("Initializing connection=[%s]", this));
        initializeCommunication(timeout);
        setInitialized(true);
		connect();
        setOperational(true);
    }

	@Override
	public void init(String hostname, int port, String username, String password, String encoding) {
		assertNotInitialized();
        doSetAttributes(hostname, port, username, password, encoding);
        doInitialize(DEFAULT_TIMEOUT);
	}

	@Override
	public void init(String hostname, int port, String username, String password, String encoding, int timeout) {
		assertNotInitialized();
        doSetAttributes(hostname, port, username, password, encoding);
        doInitialize(timeout);
    }
	
	@Override
	public void close() {
		assertInitialized();
		logger.info(String.format("Closing connection=[%s]", this));

        setOperational(false);
		disconnect();
        closeCommunication();
        setInitialized(false);
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

    // ----------------------------------------------------------------------------------
	// StompAccessor methods
	// ----------------------------------------------------------------------------------
	@Override
	public Frame receive() throws StompException {
		assertInitialized();
		logger.info("Receiving frame: ");
		Frame frame = wireFormat.unmarshal(reader);
		logger.info(frame);
		return frame;
	}

	@Override
	public void send(Frame frame) throws StompException {
		assertInitialized();
		logger.info("Sending frame: \n" + frame);
		wireFormat.marshal(frame, writer);
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }



	// ----------------------------------------------------------------------------------
	// Private getter and setters -> helpers
	// ----------------------------------------------------------------------------------
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected void setError(boolean error) {
        this.error = error;
    }

    protected boolean isError() {
        return this.error;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isOperational() {
        return operational;
    }

    protected void setOperational(boolean operational) {
        this.operational = operational;
    }

	// ----------------------------------------------------------------------------------
	// Helper methods - connection state verification
	// ----------------------------------------------------------------------------------	
	protected void assertInitialized() {
        if(isError()) {
            throw new StompConnectionException("Connection is broken after failure");
        }
		if(!isInitialized()) {
            this.setError(true);
			throw new StompConnectionException("Connection is not initialized");
		}
	}
	
	protected void assertNotInitialized() {
        if(isError()) {
            throw new StompConnectionException("Connection is broken after failure");
        }
		if(isInitialized()) {
            this.setError(true);
			throw new StompConnectionException("Connection is already initialized");
		}
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
            this.setError(true);
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    protected void initializeStreams(int timeout) {
       try {
           reader = new InputStreamReader(socket.getInputStream(), encoding);
           writer = new OutputStreamWriter(socket.getOutputStream(), encoding);
       } catch (UnsupportedEncodingException e) {
           this.setError(true);
           throw new EncodingException("Error during connection initialization", e);
       } catch (IOException e) {
           this.setError(true);
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
