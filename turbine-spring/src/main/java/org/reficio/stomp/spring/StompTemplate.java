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
package org.reficio.stomp.spring;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.spring.connection.ConnectionFactoryUtils;
import org.reficio.stomp.spring.connection.StompResourceHolder;
import org.reficio.stomp.spring.core.ConnectionCallback;
import org.reficio.stomp.spring.core.StompAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class StompTemplate extends StompAccessor {

    private static final transient Logger log = LoggerFactory.getLogger(StompTemplate.class);

    /**
     * Internal ResourceFactory adapter for interacting with
     * ConnectionFactoryUtils
     */
    private final StompTemplateResourceFactory transactionalResourceFactory = new StompTemplateResourceFactory();

    private boolean connectionTransacted = false;

    public void send(final String destination,
                     final FrameDecorator frameDecorator) {
        execute(new ConnectionCallback<Object>() {
            @Override
            public Object doInStomp(TransactionalConnection connection) throws StompException {
                doSend(connection, destination, frameDecorator);
                return null;
            }
        });
    }

    public Frame receive(final String destination) {
        return execute(new FrameReceiverCallback(destination));
    }

    public Frame receiveSelected(String destination, String selector) {
        return execute(new FrameReceiverCallback(destination, selector));
    }

    @Override
    public <T> T execute(ConnectionCallback<T> action) throws StompException {
        Assert.notNull(action, "Callback object must not be null");
        TransactionalConnection connection = null;
        TransactionalConnection connToClose = null;
        try {
            connection = ConnectionFactoryUtils.doGetTransactionalConnection(
                    getConnectionFactory(), this.transactionalResourceFactory);
            if (connection == null) {
                connection = createConnection();
                connToClose = connection;
            }
            if (log.isDebugEnabled()) {
                log.debug("Executing callback on Stomp Connection: "
                        + connection);
            }
            return action.doInStomp(connection);
        } finally {
            ConnectionFactoryUtils.releaseConnection(connToClose);
        }
    }

    protected void doSend(TransactionalConnection connection, String destination,
                          FrameDecorator frameDecorator) throws StompException {

        Assert.notNull(frameDecorator, "FrameDecorator must not be null");
        connection.send(destination, frameDecorator);
        // Check commit - avoid commit call within a JTA transaction.
        /*connection.isTransactional() && */
        if (this.isConnectionTransacted() && isConnectionLocallyTransacted(connection)) {
            // Transacted session created by this spring -> commit.
            connection.commit();
        }
    }

    /**
     * This implementation overrides the superclass method to use JMS 1.0.2 API.
     */
    protected TransactionalConnection createConnection() throws StompException {
        TransactionalConnection conn = getConnectionFactory().createConnection();
        if(this.isConnectionTransacted()) {
            conn.begin();
        }
        // TODO double-check
        // conn.setAutoTransactional(this.isConnectionTransacted());
        return conn;
    }

    protected boolean isConnectionLocallyTransacted(TransactionalConnection connection) {
        // TODO - analyze condition once more
        return // isConnectionTransacted() &&
                !ConnectionFactoryUtils.isConnectionTransactional(
                        connection, getConnectionFactory());
    }

    private class FrameReceiverCallback implements ConnectionCallback<Frame> {
        private final String destination;
        private final String selector;

        public FrameReceiverCallback(String destination) {
            this(destination, null);
        }

        public FrameReceiverCallback(String destination, String selector) {
            this.destination = destination;
            this.selector = selector;
        }

        @Override
        public Frame doInStomp(TransactionalConnection connection) throws StompException {
            String subscriptionId = null;
            try {
                if (selector == null) {
                    subscriptionId = connection.subscribe(destination);
                } else {
                    subscriptionId = connection.subscribe(destination, new FrameDecorator() {
                        @Override
                        public void decorateFrame(Frame frame) {
                            frame.selector(selector);
                        }
                    });
                }
                return connection.receive();
            } finally {
                // TODO - check this part, because if error occurs the connection shouldn't be reused
                // cleanup
                if (subscriptionId != null) {
                    connection.unsubscribe(subscriptionId);
                }
            }
        }
    }

    /**
     * ResourceFactory implementation that delegates to this spring's
     * protected callback methods.
     */
    private class StompTemplateResourceFactory implements
            ConnectionFactoryUtils.ResourceFactory {

        public TransactionalConnection getConnection(StompResourceHolder holder) {
            return StompTemplate.this.getConnection(holder);
        }

        public TransactionalConnection createConnection() throws StompException {
            TransactionalConnection conn = StompTemplate.this.createConnection();
            // TODO double-check
            // conn.setAutoTransactional(isSynchedLocalTransactionAllowed());
            // conn.setReceptionTransactional(isReceptionTransactionAllowed());
            return conn;
        }

        @Override
        public boolean isSynchedLocalTransactionAllowed() {
            return StompTemplate.this.isConnectionTransacted();
        }

    }

    public boolean isConnectionTransacted() {
        return this.connectionTransacted;
    }

    public void setConnectionTransacted(boolean connectionTransacted) {
        this.connectionTransacted = connectionTransacted;
    }

}

// COMMENTS:
// AcitveMQ, no transactional message receipt - if client ack, tx and and abort
//  -> means only that the ACK is not delivered, message will not be delivered to a different client!
//  -> message will be delivered if client closes connection before sending ack

// Consume one message only
//  -> subsciribe, receive exactly one, unsubscribe -> if more messages sent to the client, will be redelivered after unsubscribe
