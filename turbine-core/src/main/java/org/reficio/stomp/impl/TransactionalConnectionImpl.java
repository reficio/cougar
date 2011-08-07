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

import org.reficio.stomp.StompException;
import org.reficio.stomp.StompIllegalTransactionStateException;
import org.reficio.stomp.StompInvalidHeaderException;
import org.reficio.stomp.core.StompTransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class TransactionalConnectionImpl extends ConnectionImpl implements StompTransactionalConnection {

    private static final transient Logger log = LoggerFactory.getLogger(TransactionalConnectionImpl.class);

    protected String transactionId;

//    protected boolean autoAcknowledge = true;

    protected TransactionalConnectionImpl() {
        super();
    }

    // ----------------------------------------------------------------------------------
	// Factory methods
	// ----------------------------------------------------------------------------------
    public static TransactionalConnectionImpl create() {
        return new TransactionalConnectionImpl();
    }
    @Override
    public TransactionalConnectionImpl hostname(String hostname) {
        return (TransactionalConnectionImpl)super.hostname(hostname);
    }

    @Override
    public TransactionalConnectionImpl port(int port) {
        return (TransactionalConnectionImpl)super.port(port);
    }

    @Override
    public TransactionalConnectionImpl username(String username) {
        return (TransactionalConnectionImpl)super.username(username);
    }

    @Override
    public TransactionalConnectionImpl password(String password) {
        return (TransactionalConnectionImpl)super.password(password);
    }

    @Override
    public TransactionalConnectionImpl encoding(String encoding) {
        return (TransactionalConnectionImpl)super.encoding(encoding);
    }

    @Override
    public TransactionalConnectionImpl timeout(int timeout) {
        return (TransactionalConnectionImpl)super.timeout(timeout);
    }

//    @Override
//    public TransactionalConnectionImpl autoAcknowledge(boolean autoAcknowledge) {
//        assertNew();
//        setAutoAcknowledge(autoAcknowledge);
//        return this;
//    }


    // ----------------------------------------------------------------------------------
    // Overridden transaction-aware methods
    // ----------------------------------------------------------------------------------
    // TODO be aware that ack acknowledges all previous not-acknowledged messages too
    @Override
    public void ack(String messageId) {
        // beginTransactionIfRequired();
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator();
        super.ack(messageId, txDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        // beginTransactionIfRequired();
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.ack(messageId, txDecorator);
    }

    @Override
    public void send(String destination, final FrameDecorator frameDecorator) throws StompException {
        // beginTransactionIfRequired();
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.send(destination, txDecorator);
    }


//    @Override
//    public Frame receive() throws StompException {
//        // beginTransactionIfRequired();
//        Frame frame = super.receive();
////        if (isInitialized() && isAutoAcknowledge() && frame.getCommand().equals(CommandType.MESSAGE)) {
////            // ack will contain transactionID due to the method override
////            ack(frame.messageId());
////        }
//        return frame;
//    }


//    // ----------------------------------------------------------------------------------
//    // Subscribe methods override - in order to set CLIENT ack mode
//    // ----------------------------------------------------------------------------------
//    @Override
//    public String subscribe(String destination, FrameDecorator frameDecorator) {
//        ClientModeSubscriptionDecorator ackDecorator = new ClientModeSubscriptionDecorator(frameDecorator);
//        return super.subscribe(destination, ackDecorator);
//    }
//
//    @Override
//    public String subscribe(String id, String destination, FrameDecorator frameDecorator) throws StompException {
//        ClientModeSubscriptionDecorator ackDecorator = new ClientModeSubscriptionDecorator(frameDecorator);
//        return super.subscribe(id, destination, ackDecorator);
//    }

    // ----------------------------------------------------------------------------------
    // StompTransactionalConnection methods - also transaction-aware :)
    // ----------------------------------------------------------------------------------
    @Override
    public void begin() {
        assertNotInTransaction();
        this.transactionId = UUID.randomUUID().toString();
        log.info(String.format("Beginning transaction id=[%s]", transactionId));
        try {
            begin(transactionId);
        } catch (RuntimeException ex) {
            this.transactionId = null;
            throw ex;
        }
    }

    @Override
    public void rollback(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(CommandType.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public void rollback() throws StompException {
        abort(transactionId, emptyDecorator);
    }

    @Override
    public void commit(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(CommandType.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public void commit() throws StompException {
        assertInTransaction();
        log.info(String.format("Committing transaction id=[%s]", transactionId));
        commit(transactionId);
    }

//    @Override
//    public boolean isAutoAcknowledge() {
//        return this.autoAcknowledge;
//    }
//
//    protected void setAutoAcknowledge(boolean autoAcknowledge) throws StompException {
//        this.autoAcknowledge = autoAcknowledge;
//    }

    // ----------------------------------------------------------------------------------
    // Helper methods - connection state verification
    // ----------------------------------------------------------------------------------
    protected boolean isInTransaction() {
        return this.transactionId != null;
    }

    protected void assertInTransaction() {
        if (isInTransaction() == false) {
            throw new StompIllegalTransactionStateException("Transaction has not begun");
        }
    }

    protected void assertNotInTransaction() {
        if (isInTransaction() == true) {
            throw new StompIllegalTransactionStateException("Transaction has begun");
        }
    }

    // ----------------------------------------------------------------------------------
    // Transaction handling helpers
    // ----------------------------------------------------------------------------------
//    private void beginTransactionIfRequired() {
//        if (isInTransaction() == false && isInitialized() == true) {
//            if (isTransactional() == true) {
//                begin();
//            } else {
//                throw new StompIllegalTransactionStateException("Transaction has not begun");
//            }
//        }
//    }

    class TransactionAwareDecorator implements FrameDecorator {
        public TransactionAwareDecorator() {
            this.originalDecorator = null;
        }

        public TransactionAwareDecorator(final FrameDecorator originalDecorator) {
            this.originalDecorator = originalDecorator;
        }

        private FrameDecorator originalDecorator;

        @Override
        public void decorateFrame(Frame frame) {
            if (originalDecorator != null) {
                originalDecorator.decorateFrame(frame);
            }
            if (frame.transaction() != null) {
                throw new StompInvalidHeaderException("TransactionId header can't be set manually in transactional connection");
            }
            frame.transaction(transactionId);
        }
    }

//    static class ClientModeSubscriptionDecorator implements FrameDecorator {
//        public ClientModeSubscriptionDecorator(final FrameDecorator originalDecorator) {
//            this.originalDecorator = originalDecorator;
//        }
//
//        private FrameDecorator originalDecorator;
//
//        @Override
//        public void decorateFrame(Frame frame) {
//            originalDecorator.decorateFrame(frame);
//            if (frame.ack() != null) {
//                throw new StompInvalidHeaderException("AckType header can't be set manually in transactional connection - implicitly set to CLIENT");
//            }
//            frame.ack(AckType.CLIENT);
//        }
//    }

}
