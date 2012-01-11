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
package org.reficio.stomp.spring.core;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.spring.connection.StompResourceHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class StompAccessor implements InitializingBean {

	private volatile ConnectionFactory<TransactionalConnection> connectionFactory;
	
	protected abstract <T> T execute(ConnectionCallback<T> action) throws StompException;
	
	public void setConnectionFactory(ConnectionFactory<TransactionalConnection> connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory<TransactionalConnection> getConnectionFactory() {
		return this.connectionFactory;
	}

	public void afterPropertiesSet() {
		Assert.notNull(this.connectionFactory, "ConnectionFactory is required");
	}

	protected TransactionalConnection getConnection(StompResourceHolder holder) {
		return holder.getConnection();
	}
	
}
