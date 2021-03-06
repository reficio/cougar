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

package org.reficio.cougar.impl;

import org.reficio.cougar.StompException;
import org.reficio.cougar.StompIllegalTransactionStateException;
import org.reficio.cougar.StompInvalidHeaderException;
import org.reficio.cougar.connection.TransactionalClient;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.core.FramePreprocessor;
import org.reficio.cougar.core.StompWireFormat;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;
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
class TransactionalClientImpl extends ClientImpl implements TransactionalClient {

    private static final transient Logger log = LoggerFactory.getLogger(TransactionalClientImpl.class);

    protected String transactionId;

    TransactionalClientImpl(StompWireFormat wireFormat, FramePreprocessor preprocessor) {
        super(wireFormat, preprocessor);
    }

    public void postConstruct() {
        super.postConstruct();
    }

    // ----------------------------------------------------------------------------------
    // Overridden transaction-aware methods
    // ----------------------------------------------------------------------------------
    // Be aware that ack acknowledges all previous not-acknowledged messages too
    @Override
    public void ack(String messageId) {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator();
        super.ack(messageId, txDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.ack(messageId, txDecorator);
    }

    @Override
    public void send(String destination, final FrameDecorator frameDecorator) throws StompException {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.send(destination, txDecorator);
    }

    // ----------------------------------------------------------------------------------
    // StompTransactionalConnection methods - also transaction-aware :)
    // ----------------------------------------------------------------------------------
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

    public void rollback(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(Command.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void rollback() throws StompException {
        abort(transactionId, emptyDecorator);
    }

    public void commit(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(Command.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void commit() throws StompException {
        assertInTransaction();
        log.info(String.format("Committing transaction id=[%s]", transactionId));
        commit(transactionId);
    }

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


}
