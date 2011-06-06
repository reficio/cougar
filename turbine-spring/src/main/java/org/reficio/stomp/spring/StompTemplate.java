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
import org.reficio.stomp.connection.TxConnection;
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
			public Object doInStomp(TxConnection connection) throws StompException {
				doSend(connection, destination, frameDecorator);
				return null;
			}
		});
	}

	// unsubscribe from previous subscription
	// always use client ack mode in order to discard messages from previous
	// subscription
	// subscribe
	public Frame receive(String destination) {
		execute(new ConnectionCallback<Object>() {
			@Override
			public Object doInStomp(TxConnection connection) throws StompException {
				return connection.receive();
			}
		});
		return null;
	}

	public Frame receiveSelected(String destination, String selector) {
		execute(new ConnectionCallback<Object>() {
			@Override
			public Object doInStomp(TxConnection connection) throws StompException {
				// TODO Auto-generated method stub
				return null;
			}
		});
		return null;
	}

	@Override
	public <T> T execute(ConnectionCallback<T> action) throws StompException {
		Assert.notNull(action, "Callback object must not be null");
		TxConnection connection = null;
		TxConnection connToClose = null;
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

	protected void doSend(TxConnection connection, String destination,
			FrameDecorator frameDecorator) throws StompException {

		Assert.notNull(frameDecorator, "FrameDecorator must not be null");
		connection.send(destination, frameDecorator);
		// Check commit - avoid commit call within a JTA transaction.
		if (connection.getAutoTransactional() && isConnectionLocallyTransacted(connection)) {
			// Transacted session created by this spring -> commit.
			connection.commit();
		}
	}

	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 */
	protected TxConnection createConnection() throws StompException {
		TxConnection conn = getConnectionFactory().createConnection();
		conn.setAutoTransactional(this.isConnectionTransacted());
		return conn;
	}

	/**
	 * Return whether the JMS {@link Session sessions} used by this accessor are
	 * supposed to be transacted.
	 * 
	 * @see #setSessionTransacted(boolean)
	 */
	public boolean isConnectionTransacted() {
		return this.connectionTransacted;
	}

	protected boolean isConnectionLocallyTransacted(TxConnection connection) {
		// TODO - analyze condition once more
		return // isConnectionTransacted() && 
				!ConnectionFactoryUtils.isConnectionTransactional(
						connection, getConnectionFactory());
	}

	/**
	 * ResourceFactory implementation that delegates to this spring's
	 * protected callback methods.
	 */
	private class StompTemplateResourceFactory implements
			ConnectionFactoryUtils.ResourceFactory {

		public TxConnection getConnection(StompResourceHolder holder) {
			return StompTemplate.this.getConnection(holder);
		}

		public TxConnection createConnection() throws StompException {
			TxConnection conn = StompTemplate.this.createConnection();
			conn.setAutoTransactional(isSynchedLocalTransactionAllowed());
			return conn;
		}

		public boolean isSynchedLocalTransactionAllowed() {
			return StompTemplate.this.isConnectionTransacted();
		}
	}

	public void setConnectionTransacted(boolean connectionTransacted) {
		this.connectionTransacted = connectionTransacted;
	}

}
