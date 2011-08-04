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
import org.reficio.stomp.StompException;
import org.reficio.stomp.StompReceptionRollbackException;
import org.reficio.stomp.connection.TurbineTransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class TurbineTxConnectionImpl extends StompTxConnectionImpl implements TurbineTransactionalConnection {

    private static final transient Logger log = LoggerFactory.getLogger(TurbineTxConnectionImpl.class);

    private boolean transactionalReception = true;
    private String redeliveryDestination = "";

    protected TurbineTxConnectionImpl() {
        super();
        super.setAutoTransactional(true);
        super.setAutoAcknowledge(true);
    }

    // ----------------------------------------------------------------------------------
    // Factory methods
    // ----------------------------------------------------------------------------------
    public static TurbineTxConnectionImpl create() {
        return new TurbineTxConnectionImpl();
    }

    @Override
    public TurbineTxConnectionImpl autoTransactional(boolean autoTransactional) {
        throw new StompConnectionException("This option cannot be mutated - implicitly set to true");
    }

    @Override
    public TurbineTxConnectionImpl autoAcknowledge(boolean autoAcknowledge) {
        throw new StompConnectionException("This option cannot be mutated - implicitly set to true");
    }

    @Override
    public TurbineTxConnectionImpl receptionTransactional(boolean receptionTransactional) {
        assertNew();
        setReceptionTransactional(receptionTransactional);
        return this;
    }

    // TODO make sure to make received frames immutable
    List<Frame> receivedInTransaction = new ArrayList<Frame>();

    public List<Frame> getReceivedInCurrentTransaction() {
        if (isInitialized() == true && isInTransaction() == true) {
            return new ArrayList<Frame>(receivedInTransaction);
        } else {
            return null;
        }
    }

    private void resetReceivedInCurrentTransaction() {
        receivedInTransaction.clear();
    }

    private void rollbackTransactionalReception() {
        // do not create transaction if no messages received
        if (receivedInTransaction.size() == 0 || transactionalReception == false) {
            return;
        }

        try {
            String transactionId = UUID.randomUUID().toString();
            super.begin(transactionId);
            for (Frame frame : receivedInTransaction) {
                frame.transaction(transactionId);
                frame.custom("redelivered", "true");
                if (redeliveryDestination != null) {
                    frame.destination(redeliveryDestination);
                }
                super.send(frame);
            }
            super.commit(transactionId);
            resetReceivedInCurrentTransaction();
        } catch (StompException ex) {
            new StompReceptionRollbackException("Error during reception rollback", ex, receivedInTransaction);
        }
    }

    // IMPORTANT!!! DO NOT USE TRANSACTIONS WHILE SENDING ACK
    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.ACK);
        frame.messageId(messageId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    // IMPORTANT!!! DO NOT USE TRANSACTIONS WHILE SENDING ACK
    @Override
    public void ack(String messageId) {
        ack(messageId, emptyDecorator);
    }


    @Override
    public Frame receive() throws StompException {
        Frame frame = super.receive();
        if (transactionalReception == true) {
            receivedInTransaction.add(frame);
        }
        return frame;
    }

    @Override
    public void rollback(FrameDecorator frameDecorator) throws StompException {
        super.rollback(frameDecorator);
        rollbackTransactionalReception();
    }

    @Override
    public void rollback() throws StompException {
        super.rollback();
        rollbackTransactionalReception();
    }

    @Override
    public void commit(FrameDecorator frameDecorator) throws StompException {
        super.commit(frameDecorator);
        resetReceivedInCurrentTransaction();
    }

    @Override
    public void commit() throws StompException {
        super.commit();
        resetReceivedInCurrentTransaction();
    }

    @Override
    public boolean isReceptionTransactional() {
        return this.transactionalReception;
    }

    protected void setReceptionTransactional(boolean receptionTransactional) throws StompException {
        this.transactionalReception = receptionTransactional;
    }

}
