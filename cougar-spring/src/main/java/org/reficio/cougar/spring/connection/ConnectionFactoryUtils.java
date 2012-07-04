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
package org.reficio.cougar.spring.connection;

import org.reficio.cougar.StompException;
import org.reficio.cougar.connection.ConnectionFactory;
import org.reficio.cougar.connection.TransactionalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class ConnectionFactoryUtils {

	private static final transient Logger log = LoggerFactory.getLogger(ConnectionFactoryUtils.class);
	
	public static void releaseConnection(TransactionalClient client) {
		if (client == null) {
			return;
		}
		try {
			client.close();
		}
		catch (Throwable ex) {
			log.debug("Could not close Stomp Client", ex);
		}
	}
	
	/**
	 * Determine whether the given JMS Session is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param cf the JMS ConnectionFactory that the Session originated from
	 * @return whether the Session is transactional
	 */
	public static boolean isConnectionTransactional(TransactionalClient client, ConnectionFactory<TransactionalClient> cf) {
		if (client == null || cf == null) {
			return false;
		}
		StompResourceHolder resourceHolder = (StompResourceHolder) TransactionSynchronizationManager.getResource(cf);
		return (resourceHolder != null && resourceHolder.containsConnection(client));
	}
	
	public static TransactionalClient getTransactionalConnection(
			final ConnectionFactory<TransactionalClient> cf, final boolean synchedLocalTransactionAllowed, final boolean receptionTransactional) {

		return doGetTransactionalConnection(cf, new ResourceFactory() {
			public TransactionalClient getConnection(StompResourceHolder holder) {
				return (holder.getConnection());
			}
			public TransactionalClient createConnection() {
				TransactionalClient conn = cf.createConnection();
                // TODO double-check
				// conn.setAutoTransactional(synchedLocalTransactionAllowed);
                // conn.setReceptionTransactional(receptionTransactional);
				return conn;
			}
			public boolean isSynchedLocalTransactionAllowed() {
				return synchedLocalTransactionAllowed;
			}

            public boolean isReceptionTransactionAllowed() {
				return receptionTransactional;
			}
		});
	}
	
	/**
	 * Obtain a JMS Session that is synchronized with the current transaction, if any.
	 * @param connectionFactory the JMS ConnectionFactory to bind for
	 * (used as TransactionSynchronizationManager key)
	 * @param resourceFactory the ResourceFactory to use for extracting or creating
	 * JMS resources
	 * @param startConnection whether the underlying JMS Client approach should be
	 * started in order to allow for receiving messages. Note that a reused Client
	 * may already have been started before, even if this flag is <code>false</code>.
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static TransactionalClient doGetTransactionalConnection(
			ConnectionFactory<TransactionalClient> connectionFactory, ResourceFactory resourceFactory) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
		Assert.notNull(resourceFactory, "ResourceFactory must not be null");

		StompResourceHolder resourceHolder =
				(StompResourceHolder) TransactionSynchronizationManager.getResource(connectionFactory);
		if (resourceHolder != null) {
			TransactionalClient client = resourceFactory.getConnection(resourceHolder);
			if (client != null) {
				return client;
			}
			if (resourceHolder.isFrozen()) {
				return null;
			}
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return null;
		}
		StompResourceHolder resourceHolderToUse = resourceHolder;
		if (resourceHolderToUse == null) {
			resourceHolderToUse = new StompResourceHolder();
		}
		TransactionalClient client = resourceFactory.getConnection(resourceHolderToUse);
		try {
			boolean isExistingCon = (client != null);
			if (!isExistingCon) {
				client = resourceFactory.createConnection();
				resourceHolderToUse.addConnection(client);
			}
		}
		catch (StompException ex) {
			if (client != null) {
				try {
					client.close();
				}
				catch (Throwable ex2) {
					// ignore
				}
			}
			throw ex;
		}
		if (resourceHolderToUse != resourceHolder) {
			TransactionSynchronizationManager.registerSynchronization(
					new StompResourceSynchronization(
							resourceHolderToUse, connectionFactory, resourceFactory.isSynchedLocalTransactionAllowed()));
			resourceHolderToUse.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.bindResource(connectionFactory, resourceHolderToUse);
		}
		return client;
	}
	
	
	/**
	 * Callback interface for resource creation.
	 * Serving as argument for the <code>doGetTransactionalSession</code> method.
	 */
	public interface ResourceFactory {

		/**
		 * Fetch an appropriate Client from the given StompResourceHolder.
		 * @param holder the StompResourceHolder
		 * @return an appropriate Client fetched from the holder,
		 * or <code>null</code> if none found
		 */
		TransactionalClient getConnection(StompResourceHolder holder);

		/**
		 * Create a new JMS Client for registration with a StompResourceHolder.
		 * @return the new JMS Client
		 * @throws JMSException if thrown by JMS API methods
		 */
		TransactionalClient createConnection();

		/**
		 * Return whether to allow for a local JMS transaction that is synchronized with
		 * a Spring-managed transaction (where the main transaction might be a JDBC-based
		 * one for a specific DataSource, for example), with the JMS transaction
		 * committing right after the main transaction.
		 * @return whether to allow for synchronizing a local JMS transaction
		 */
		boolean isSynchedLocalTransactionAllowed();

	}

	
	
	/**
	 * Callback for resource cleanup at the end of a non-native JMS transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class StompResourceSynchronization extends ResourceHolderSynchronization<StompResourceHolder, Object> {

		private final boolean transacted;

		public StompResourceSynchronization(StompResourceHolder resourceHolder, Object resourceKey, boolean transacted) {
			super(resourceHolder, resourceKey);
			this.transacted = transacted;
		}

		protected boolean shouldReleaseBeforeCompletion() {
			return !this.transacted;
		}

		protected void processResourceAfterCommit(StompResourceHolder resourceHolder) {
			resourceHolder.commitAll();
		}

		protected void releaseResource(StompResourceHolder resourceHolder, Object resourceKey) {
			resourceHolder.closeAll();
		}
	}


}
