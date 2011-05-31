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
import org.reficio.stomp.StompIllegalTransactionStateException;
import org.reficio.stomp.StompInvalidHeaderException;
import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.TxConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.AckType;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;

import java.util.UUID;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class TxConnectionImpl extends ConnectionImpl implements TxConnection {

	private static final Log logger = LogFactory.getLog(TxConnectionImpl.class);
	
	private String transactionId;
	private boolean autoTransactional;
	
	public TxConnectionImpl() {
		super();
	}

	// ----------------------------------------------------------------------------------
	// Overridden transaction-aware methods
	// ----------------------------------------------------------------------------------
	@Override
	public void ack(String messageId, FrameDecorator frameDecorator) {
		beginTransactionIfRequired();
		TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
		super.ack(messageId, txDecorator);
	}

	@Override
	public void send(String destination, final FrameDecorator frameDecorator) throws StompException {
		beginTransactionIfRequired();
		TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
		super.send(destination, txDecorator);
	}
	
	@Override
	public Frame receive() throws StompException {
		beginTransactionIfRequired();
		Frame frame = super.receive();
	    // ack will contain transactionID due to the method override
        // do not send ack if it is the connect/connected message sequence
        if(isInitialized()) {
		    ack(frame.messageId());
        }
		return frame;
	}
	

    // ----------------------------------------------------------------------------------
	// Subscribe methods override - in order to set CLIENT ack mode
	// ----------------------------------------------------------------------------------
    @Override
	public String subscribe(String destination, FrameDecorator frameDecorator) {
        ClientModeSubscriptionDecorator ackDecorator = new ClientModeSubscriptionDecorator(frameDecorator);
        return super.subscribe(destination, ackDecorator);
	}

    @Override
    public String subscribe(String id, String destination, FrameDecorator frameDecorator) throws StompException {
        ClientModeSubscriptionDecorator ackDecorator = new ClientModeSubscriptionDecorator(frameDecorator);
        return super.subscribe(id, destination, ackDecorator);
    }

	// ----------------------------------------------------------------------------------
	// StompTransactionalConnection methods - also transaction-aware :)
	// ----------------------------------------------------------------------------------
	public void begin() {
		assertNotInTransaction();
		this.transactionId = UUID.randomUUID().toString();
		logger.info(String.format("Beginnig transaction id=[%s]", transactionId));
		try {
            begin(transactionId);
        } catch(RuntimeException ex) {
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
		logger.info(String.format("Committing transaction id=[%s]", transactionId));
		commit(transactionId);
	}
	
	@Override
	public boolean getAutoTransactional() {
		return this.autoTransactional;
	}

	@Override
	public void setAutoTransactional(boolean transactional) throws StompException {
		assertNotInTransaction();
		this.autoTransactional = transactional;
	}
	
	// ----------------------------------------------------------------------------------
	// Helper methods - connection state verification
	// ----------------------------------------------------------------------------------	
	private boolean isInTransaction() {
		return this.transactionId!=null;
	}
	
	private void assertInTransaction() {
		if(isInTransaction() == false) {
			throw new StompIllegalTransactionStateException("Transaction has not begun");
		}
	}
	
	private void assertNotInTransaction() {
		if(isInTransaction() == true) {
			throw new StompIllegalTransactionStateException("Transaction has begun");
		}
	}

	// ----------------------------------------------------------------------------------
	// Transaction handling helpers
	// ----------------------------------------------------------------------------------
	private void beginTransactionIfRequired() {
		if(getAutoTransactional()==true && isInTransaction()==false && isInitialized()==true) {
			begin();
		}
	}
	
	class TransactionAwareDecorator implements FrameDecorator {
		public TransactionAwareDecorator(final FrameDecorator originalDecorator) {
			this.originalDecorator = originalDecorator;
		}
		
		private FrameDecorator originalDecorator;
		
		@Override
		public void decorateFrame(Frame frame) {
			originalDecorator.decorateFrame(frame);
			if(frame.transaction()!= null) {
				throw new StompInvalidHeaderException("TransactionId header can't be set manually in transactional connection");
			}
			frame.transaction(transactionId);
		}
	}

    static class ClientModeSubscriptionDecorator implements FrameDecorator {
		public ClientModeSubscriptionDecorator(final FrameDecorator originalDecorator) {
			this.originalDecorator = originalDecorator;
		}

		private FrameDecorator originalDecorator;

		@Override
		public void decorateFrame(Frame frame) {
			originalDecorator.decorateFrame(frame);
			if(frame.ack()!= null) {
				throw new StompInvalidHeaderException("AckType header can't be set manually in transactional connection - implicitly set to CLIENT");
			}
			frame.ack(AckType.CLIENT);
		}
	}

	
}
