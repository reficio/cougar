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
package org.reficio.stomp.spring.connection;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.connection.TransactionalConnection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class StompTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager, InitializingBean {

	private static final long serialVersionUID = 1L;

	private ConnectionFactory<TransactionalConnection> connectionFactory;


	/**
	 * Create a new RabbitTransactionManager for bean-style usage.
	 * <p>
	 * Note: The ConnectionFactory has to be set before using the instance. This constructor can be used to prepare a
	 * RabbitTemplate via a BeanFactory, typically setting the ConnectionFactory via setConnectionFactory.
	 * <p>
	 * Turns off transaction synchronization by default, as this manager might be used alongside a datastore-based
	 * Spring transaction manager like DataSourceTransactionManager, which has stronger needs for synchronization. Only
	 * one manager is allowed to drive synchronization at any point of time.
	 * @see #setConnectionFactory
	 * @see #setTransactionSynchronization
	 */
	public StompTransactionManager() {
		setTransactionSynchronization(SYNCHRONIZATION_NEVER);
	}

	/**
	 * Create a new RabbitTransactionManager, given a ConnectionFactory.
	 * @param connectionFactory the ConnectionFactory to use
	 */
	public StompTransactionManager(ConnectionFactory<TransactionalConnection> connectionFactory) {
		this();
		this.connectionFactory = connectionFactory;
		afterPropertiesSet();
	}

	/**
	 * @param cf the connectionFactory to set
	 */
	public void setConnectionFactory(ConnectionFactory<TransactionalConnection> cf) {
		if (cf instanceof ManagedTxAwareConnectionFactoryProxy) {
			// If we got a TransactionAwareConnectionFactoryProxy, we need to perform transactions
			// for its underlying target ConnectionFactory, else JMS access code won't see
			// properly exposed transactions (i.e. transactions for the target ConnectionFactory).
			this.connectionFactory = ((ManagedTxAwareConnectionFactoryProxy) cf).getTargetConnectionFactory();
		}
		else {
			this.connectionFactory = cf;
		}
	}

	/**
	 * @return the connectionFactory
	 */
	public ConnectionFactory<TransactionalConnection> getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Make sure the ConnectionFactory has been set.
	 */
	public void afterPropertiesSet() {
		if (getConnectionFactory() == null) {
			throw new IllegalArgumentException("Property 'connectionFactory' is required");
		}
	}

	public Object getResourceFactory() {
		return getConnectionFactory();
	}

	protected Object doGetTransaction() {
		StompTransactionObject txObject = new StompTransactionObject();
		txObject.setResourceHolder((StompResourceHolder) TransactionSynchronizationManager
				.getResource(getConnectionFactory()));
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		StompTransactionObject txObject = (StompTransactionObject) transaction;
		return (txObject.getResourceHolder() != null);
	}
	
	protected TransactionalConnection createConnection() {
		TransactionalConnection conn = connectionFactory.createConnection();
        // TODO double-check
		// always use transactional connection
		// conn.setAutoTransactional(true);
        // conn.setReceptionTransactional(true);
		return conn;
	}
	
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("AMQP does not support an isolation level concept");
		}
		StompTransactionObject txObject = (StompTransactionObject) transaction;
		TransactionalConnection connection = null;
		try {					
			connection = createConnection();
            connection.begin();
			if (logger.isDebugEnabled()) {
				logger.debug("Created JMS transaction on Connection [" + connection + "]");
			}
			txObject.setResourceHolder(new StompResourceHolder(connection));
			txObject.getResourceHolder().setSynchronizedWithTransaction(true);
			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getResourceHolder().setTimeoutInSeconds(timeout);
			}
			TransactionSynchronizationManager.bindResource(
					getConnectionFactory(), txObject.getResourceHolder());
		} catch (StompException ex) {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (Throwable ex2) {
					// ignore
				}
			}
			throw new CannotCreateTransactionException("Could not create Stomp transaction", ex);
		}
	}
	protected Object doSuspend(Object transaction) {
		StompTransactionObject txObject = (StompTransactionObject) transaction;
		txObject.setResourceHolder(null);
		return TransactionSynchronizationManager.unbindResource(getConnectionFactory());
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		StompResourceHolder conHolder = (StompResourceHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(getConnectionFactory(), conHolder);
	}

	protected void doCommit(DefaultTransactionStatus status) {
		StompTransactionObject txObject = (StompTransactionObject) status.getTransaction();
		TransactionalConnection connection = txObject.getResourceHolder().getConnection();
		try {
			if (status.isDebug()) {
				logger.debug("Committing Stomp transaction on Connection [" + connection + "]");
			}
			connection.commit();
		}
		catch (StompException ex) {
			throw new TransactionSystemException("Could not commit Stomp transaction", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		StompTransactionObject txObject = (StompTransactionObject) status.getTransaction();
		TransactionalConnection connection = txObject.getResourceHolder().getConnection();
		try {
			if (status.isDebug()) {
				logger.debug("Rolling back Stomp transaction on Connection [" + connection + "]");
			}
			connection.rollback();
		}
		catch (StompException ex) {
			throw new TransactionSystemException("Could not roll back Stomp transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		StompTransactionObject txObject = (StompTransactionObject) status.getTransaction();
		txObject.getResourceHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		StompTransactionObject txObject = (StompTransactionObject) transaction;
		TransactionSynchronizationManager.unbindResource(getConnectionFactory());
		txObject.getResourceHolder().closeAll();
		txObject.getResourceHolder().clear();
	}

	/**
	 * Rabbit transaction object, representing a RabbitResourceHolder. Used as transaction object by
	 * RabbitTransactionManager.
	 */
	private static class StompTransactionObject implements SmartTransactionObject {

		private StompResourceHolder resourceHolder;

		public void setResourceHolder(StompResourceHolder resourceHolder) {
			this.resourceHolder = resourceHolder;
		}

		public StompResourceHolder getResourceHolder() {
			return this.resourceHolder;
		}

		public boolean isRollbackOnly() {
			return this.resourceHolder.isRollbackOnly();
		}

		public void flush() {
			// no-op
		}
	}

	
}
