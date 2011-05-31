package org.reficio.stomp.spring.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.connection.TxConnection;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class ConnectionFactoryUtils {

	private static final Log logger = LogFactory.getLog(ConnectionFactoryUtils.class);
	
	public static void releaseConnection(TxConnection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.close();
		}
		catch (Throwable ex) {
			logger.debug("Could not close Stomp Connection", ex);
		}
	}
	
	/**
	 * Determine whether the given JMS Session is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param session the JMS Session to check
	 * @param cf the JMS ConnectionFactory that the Session originated from
	 * @return whether the Session is transactional
	 */
	public static boolean isConnectionTransactional(TxConnection connection, ConnectionFactory<TxConnection> cf) {
		if (connection == null || cf == null) {
			return false;
		}
		StompResourceHolder resourceHolder = (StompResourceHolder) TransactionSynchronizationManager.getResource(cf);
		return (resourceHolder != null && resourceHolder.containsConnection(connection));
	}
	
	public static TxConnection getTransactionalConnection(
			final ConnectionFactory<TxConnection> cf, final boolean synchedLocalTransactionAllowed) {

		return doGetTransactionalConnection(cf, new ResourceFactory() {
			public TxConnection getConnection(StompResourceHolder holder) {
				return (holder.getConnection());
			}
			public TxConnection createConnection() {
				TxConnection conn = cf.createConnection();
				conn.setAutoTransactional(synchedLocalTransactionAllowed);
				return conn;
				
			}
			public boolean isSynchedLocalTransactionAllowed() {
				return synchedLocalTransactionAllowed;
			}
		});
	}
	
	/**
	 * Obtain a JMS Session that is synchronized with the current transaction, if any.
	 * @param connectionFactory the JMS ConnectionFactory to bind for
	 * (used as TransactionSynchronizationManager key)
	 * @param resourceFactory the ResourceFactory to use for extracting or creating
	 * JMS resources
	 * @param startConnection whether the underlying JMS Connection approach should be
	 * started in order to allow for receiving messages. Note that a reused Connection
	 * may already have been started before, even if this flag is <code>false</code>.
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static TxConnection doGetTransactionalConnection(
			ConnectionFactory<TxConnection> connectionFactory, ResourceFactory resourceFactory) {

		Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
		Assert.notNull(resourceFactory, "ResourceFactory must not be null");

		StompResourceHolder resourceHolder =
				(StompResourceHolder) TransactionSynchronizationManager.getResource(connectionFactory);
		if (resourceHolder != null) {
			TxConnection connection = resourceFactory.getConnection(resourceHolder);
			if (connection != null) {
				return connection;
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
		TxConnection connection = resourceFactory.getConnection(resourceHolderToUse);
		try {
			boolean isExistingCon = (connection != null);
			if (!isExistingCon) {
				connection = resourceFactory.createConnection();
				resourceHolderToUse.addConnection(connection);
			}
		}
		catch (StompException ex) {
			if (connection != null) {
				try {
					connection.close();
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
		return connection;
	}
	
	
	/**
	 * Callback interface for resource creation.
	 * Serving as argument for the <code>doGetTransactionalSession</code> method.
	 */
	public interface ResourceFactory {

		/**
		 * Fetch an appropriate Connection from the given StompResourceHolder.
		 * @param holder the StompResourceHolder
		 * @return an appropriate Connection fetched from the holder,
		 * or <code>null</code> if none found
		 */
		TxConnection getConnection(StompResourceHolder holder);

		/**
		 * Create a new JMS Connection for registration with a StompResourceHolder.
		 * @return the new JMS Connection
		 * @throws JMSException if thrown by JMS API methods
		 */
		TxConnection createConnection();

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
