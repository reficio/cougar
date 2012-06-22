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

import org.apache.commons.lang.StringUtils;
import org.reficio.stomp.*;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.*;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.reficio.stomp.core.StompResourceState.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
class ClientImpl extends StompResourceImpl implements Client {

    private static final transient Logger log = LoggerFactory.getLogger(ClientImpl.class);

    private TransmissionHandler transmissionHandler;
    private StompResourceState state;
    private final StompWireFormat wireFormat;

    public static final int INDEFINITE_RECEPTION_TIMEOUT = 0;
    public static final int NOWAIT_RECEPTION_TIMEOUT = 100;


    // ----------------------------------------------------------------------------------
    // Constructor - only for internal usage
    // ----------------------------------------------------------------------------------
    ClientImpl(StompWireFormat wireFormat) {
        this.wireFormat = wireFormat;
        setState(NEW);
    }

    public void postConstruct() {
        this.transmissionHandler = CloseTransmissionOnErrorInvocationHandler.getHandler(
                new TransmissionHandlerImpl(wireFormat, hostname, port, encoding));
    }

    void setTransmissionHandler(TransmissionHandler transmissionHandler) {
        this.transmissionHandler = transmissionHandler;
    }

    // ----------------------------------------------------------------------------------
    // StompResource methods
    // ----------------------------------------------------------------------------------
    @Override
    public void connect() {
        assertNew();
        log.info(String.format("Initializing connection=[%s]", this));
        transmissionHandler.initializeCommunication(timeout);
        setState(CONNECTING);
        doConnect();
        setState(CONNECTED);
    }

    @Override
    public void close() {
        assertOperational();
        log.info(String.format("Closing connection=[%s]", this));
        setState(CLOSING);
        disconnect();
        transmissionHandler.closeCommunication();
        setState(CLOSED);
    }

    @Override
    public boolean isConnected() {
        return state.equals(CONNECTED);
    }

    // ----------------------------------------------------------------------------------
    // Helper methods -> connection state modifiers
    // ----------------------------------------------------------------------------------
    protected void doConnect() {
        Frame frame = new Frame(Command.CONNECT);
        frame.login(username);
        frame.passcode(password);
        frame.encoding(encoding);
        transmissionHandler.marshall(frame);

        try {
            Frame serverHandshake = transmissionHandler.unmarshall();
            validateServerHandshake(serverHandshake);
            validateTransmissionEncoding(serverHandshake);
            setSessionIdFromHandshakeIfNotNull(serverHandshake);

        } catch (StompSocketTimeoutException exception) {
            closeCommunicationOnError();
            throw new StompProtocolException("Server handshake frame reception timeout -> aborting!");
        }
    }

    private void setSessionIdFromHandshakeIfNotNull(Frame serverHandshake) {
        String sessionId = serverHandshake.session();
        if (sessionId != null) {
            sessionId(sessionId);
        } else {
            log.warn("Server has not returned a session id");
        }
    }

    private void validateServerHandshake(Frame serverHandshake) {
        if (isProperServerHandshake(serverHandshake) == false) {
            closeCommunicationOnError();
            throw new StompProtocolException("Expected CONNECTED command, instead received "
                    + serverHandshake.getCommand().name());
        }
    }

    private void validateTransmissionEncoding(Frame serverHandshake) {
        if (isEncodingAccepted(serverHandshake.encoding()) == false) {
            closeCommunicationOnError();
            throw new StompEncodingException("Server cannot handle requested encoding and switched to [" +
                    serverHandshake.encoding() + "] -> aborting!");
        }
    }

    private boolean isProperServerHandshake(Frame serverHandshake) {
        return serverHandshake.getCommand().equals(Command.CONNECTED);
    }

    private boolean isEncodingAccepted(String returnedEncoding) {
        return StringUtils.isBlank(returnedEncoding) || encoding.equals(returnedEncoding);
    }

    protected void closeCommunicationOnError() {
        setState(BROKEN);
        transmissionHandler.closeCommunication();
    }

    protected void disconnect() {
        Frame frame = new Frame(Command.DISCONNECT);
        frame.session(sessionId);
        transmissionHandler.marshall(frame);
    }


    // ----------------------------------------------------------------------------------
    // StompAccessor methods
    // ----------------------------------------------------------------------------------
    @Override
    public Frame receive() throws StompException {
        transmissionHandler.setReceptionTimeoutInMillis(INDEFINITE_RECEPTION_TIMEOUT);
        assertOperational();
        return transmissionHandler.unmarshall();
    }

    @Override
    public Frame receive(int timeout) throws StompException {
        transmissionHandler.setReceptionTimeoutInMillis(timeout);
        assertOperational();
        return transmissionHandler.unmarshall();
    }

    @Override
    public Frame receiveNoWait() throws StompException {
        return receive(NOWAIT_RECEPTION_TIMEOUT);
    }

    @Override
    public void send(Frame frame) throws StompException {
        assertOperational();
        transmissionHandler.marshall(checkNotNull(frame));
    }

    // ----------------------------------------------------------------------------------
    // Helper methods -> connection state verification
    // ----------------------------------------------------------------------------------
    protected void assertOperational() {
        if (isConnected() == false) {
            throw new StompConnectionException(
                    String.format("Connection is not operational. Connection state is [%s]", getState()));
        }
    }

    protected void assertNew() {
        if (getState().equals(NEW) == false) {
            throw new StompConnectionException("Connection is not in NEW state");
        }
    }

    // ----------------------------------------------------------------------------------
    // Connection state mutators
    // ----------------------------------------------------------------------------------
    protected StompResourceState getState() {
        return this.state;
    }

    protected void setState(StompResourceState state) {
        log.info(String.format("Setting connection state to [%s]", state.name()));
        this.state = state;
    }

    private static class CloseTransmissionOnErrorInvocationHandler implements InvocationHandler {

        private final TransmissionHandler target;

        public CloseTransmissionOnErrorInvocationHandler(TransmissionHandler target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(this.target, args);
            } catch (InvocationTargetException ex) {
                Throwable targetException = ex.getTargetException();
                if ((targetException instanceof StompSocketTimeoutException) == false) {
                    target.closeCommunication();
                }
                throw targetException;
            }
        }

        public static TransmissionHandler getHandler(TransmissionHandler target) {
            List<Class> classes = new ArrayList<Class>();
            classes.add(TransmissionHandler.class);
            return (TransmissionHandler) Proxy.newProxyInstance(
                    TransmissionHandler.class.getClassLoader(),
                    classes.toArray(new Class[classes.size()]),
                    new CloseTransmissionOnErrorInvocationHandler(target));
        }
    }

}