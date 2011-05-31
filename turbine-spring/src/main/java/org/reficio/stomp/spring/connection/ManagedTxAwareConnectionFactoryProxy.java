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

/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.connection.TxConnection;
import org.springframework.jca.cci.connection.SingleConnectionFactory;
import org.springframework.util.Assert;

/**
 * Proxy for a target JMS {@link javax.jms.ConnectionFactory}, adding awareness of
 * Spring-managed transactions. Similar to a transactional JNDI ConnectionFactory
 * as provided by a J2EE server.
 *
 * <p>Messaging code which should remain unaware of Spring's JMS support can work with
 * this proxy to seamlessly participate in Spring-managed transactions. Note that the
 * transaction manager, for example {@link JmsTransactionManager}, still needs to work
 * with the underlying ConnectionFactory, <i>not</i> with this proxy.
 *
 * <p><b>Make sure that TransactionAwareConnectionFactoryProxy is the outermost
 * ConnectionFactory of a chain of ConnectionFactory proxies/adapters.</b>
 * TransactionAwareConnectionFactoryProxy can delegate either directly to the
 * target factory or to some intermediary adapter like
 * {@link UserCredentialsConnectionFactoryAdapter}.
 *
 * <p>Delegates to {@link ConnectionFactoryUtils} for automatically participating
 * in thread-bound transactions, for example managed by {@link JmsTransactionManager}.
 * <code>createSession</code> calls and <code>close</code> calls on returned Sessions
 * will behave properly within a transaction, that is, always work on the transactional
 * Session. If not within a transaction, normal ConnectionFactory behavior applies.
 *
 * <p>Note that transactional JMS Sessions will be registered on a per-Connection
 * basis. To share the same JMS Session across a transaction, make sure that you
 * operate on the same JMS Connection handle - either through reusing the handle
 * or through configuring a {@link SingleConnectionFactory} underneath.
 *
 * <p>Returned transactional Session proxies will implement the {@link SessionProxy}
 * interface to allow for access to the underlying target Session. This is only
 * intended for accessing vendor-specific Session API or for testing purposes
 * (e.g. to perform manual transaction control). For typical application purposes,
 * simply use the standard JMS Session interface.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see UserCredentialsConnectionFactoryAdapter
 * @see SingleConnectionFactory
 */
public class ManagedTxAwareConnectionFactoryProxy implements ConnectionFactory<TxConnection> {

	private boolean synchedLocalTransactionAllowed = false;

	private ConnectionFactory<TxConnection> targetConnectionFactory;


	/**
	 * Create a new TransactionAwareConnectionFactoryProxy.
	 */
	public ManagedTxAwareConnectionFactoryProxy() {
	}

	/**
	 * Create a new TransactionAwareConnectionFactoryProxy.
	 * @param targetConnectionFactory the target ConnectionFactory
	 */
	public ManagedTxAwareConnectionFactoryProxy(ConnectionFactory<TxConnection> targetConnectionFactory) {
		setTargetConnectionFactory(targetConnectionFactory);
	}


	/**
	 * Set the target ConnectionFactory that this ConnectionFactory should delegate to.
	 */
	public final void setTargetConnectionFactory(ConnectionFactory<TxConnection> targetConnectionFactory) {
		Assert.notNull(targetConnectionFactory, "targetConnectionFactory must not be nul");
		this.targetConnectionFactory = targetConnectionFactory;
	}

	/**
	 * Return the target ConnectionFactory that this ConnectionFactory should delegate to.
	 */
	protected ConnectionFactory<TxConnection> getTargetConnectionFactory() {
		return this.targetConnectionFactory;
	}

	/**
	 * Set whether to allow for a local JMS transaction that is synchronized with a
	 * Spring-managed transaction (where the main transaction might be a JDBC-based
	 * one for a specific DataSource, for example), with the JMS transaction committing
	 * right after the main transaction. If not allowed, the given ConnectionFactory
	 * needs to handle transaction enlistment underneath the covers.
	 * <p>Default is "false": If not within a managed transaction that encompasses
	 * the underlying JMS ConnectionFactory, standard Sessions will be returned.
	 * Turn this flag on to allow participation in any Spring-managed transaction,
	 * with a local JMS transaction synchronized with the main transaction.
	 */
	public void setSynchedLocalTransactionAllowed(boolean synchedLocalTransactionAllowed) {
		this.synchedLocalTransactionAllowed = synchedLocalTransactionAllowed;
	}

	/**
	 * Return whether to allow for a local JMS transaction that is synchronized
	 * with a Spring-managed transaction.
	 */
	protected boolean isSynchedLocalTransactionAllowed() {
		return this.synchedLocalTransactionAllowed;
	}


	public TxConnection createConnection() {
		TxConnection connection = ConnectionFactoryUtils.getTransactionalConnection(
				getTargetConnectionFactory(), isSynchedLocalTransactionAllowed());
		if (connection != null) {
			return getCloseSuppressingConnectionProxy(connection);
		} else {
			TxConnection conn = getTargetConnectionFactory().createConnection();
			conn.setAutoTransactional(isSynchedLocalTransactionAllowed());
			return conn;
		}
	}

	private TxConnection getCloseSuppressingConnectionProxy(TxConnection target) {
		List<Class> classes = new ArrayList<Class>(3);
		classes.add(TxConnectionProxy.class);
		return (TxConnection) Proxy.newProxyInstance(
				TxConnectionProxy.class.getClassLoader(),
				classes.toArray(new Class[classes.size()]),
				new CloseSuppressingConnectionInvocationHandler(target));
	}
	

	/**
	 * Invocation handler that suppresses close calls for a transactional JMS Session.
	 */
	private static class CloseSuppressingConnectionInvocationHandler implements InvocationHandler {

		private final TxConnection target;

		public CloseSuppressingConnectionInvocationHandler(TxConnection target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on SessionProxy interface coming in...

			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of Connection proxy.
				return System.identityHashCode(proxy);
			}
			else if (method.getName().equals("commit")) {
				throw new StompConnectionException("Commit call not allowed within a managed transaction");
			}
			else if (method.getName().equals("rollback")) {
				throw new StompConnectionException("Rollback call not allowed within a managed transaction");
			}
			else if (method.getName().equals("close")) {
				// Handle close method: not to be closed within a transaction.
				return null;
			}
			else if (method.getName().equals("getTargetConnection")) {
				// Handle getTargetSession method: return underlying Session.
				return this.target;
			}

			// Invoke method on target Session.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
