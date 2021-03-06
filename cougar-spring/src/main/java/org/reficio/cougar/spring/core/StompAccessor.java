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
package org.reficio.cougar.spring.core;

import org.reficio.cougar.StompException;
import org.reficio.cougar.connection.ConnectionFactory;
import org.reficio.cougar.connection.TransactionalClient;
import org.reficio.cougar.spring.connection.StompResourceHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class StompAccessor implements InitializingBean {

	private volatile ConnectionFactory<TransactionalClient> connectionFactory;
	
	protected abstract <T> T execute(ConnectionCallback<T> action) throws StompException;
	
	public void setConnectionFactory(ConnectionFactory<TransactionalClient> connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory<TransactionalClient> getConnectionFactory() {
		return this.connectionFactory;
	}

	public void afterPropertiesSet() {
		Assert.notNull(this.connectionFactory, "ConnectionFactory is required");
	}

	protected TransactionalClient getConnection(StompResourceHolder holder) {
		return holder.getConnection();
	}
	
}
