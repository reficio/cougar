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

import java.util.LinkedList;
import java.util.List;

import org.reficio.cougar.StompException;
import org.reficio.cougar.connection.TransactionalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class StompResourceHolder extends ResourceHolderSupport {

	private static final transient Logger log = LoggerFactory.getLogger(StompResourceHolder.class);

	private boolean frozen = false;

	private final List<TransactionalClient> clients = new LinkedList<TransactionalClient>();

	public StompResourceHolder() {
	}

	public StompResourceHolder(TransactionalClient client) {
		addConnection(client);
	}

	public final boolean isFrozen() {
		return this.frozen;
	}

	public final void addConnection(TransactionalClient client) {
		Assert.isTrue(!this.frozen, "Cannot add Client because StompResourceHolder is frozen");
		Assert.notNull(client, "Client must not be null");
		if (!this.clients.contains(client)) {
			this.clients.add(client);
		}
	}

	public TransactionalClient getConnection() {
		return (!this.clients.isEmpty() ? this.clients.get(0) : null);
	}

	public TransactionalClient getConnection(Class<? extends TransactionalClient> connectionType) {
		return CollectionUtils.findValueOfType(this.clients, connectionType);
	}

	public boolean containsConnection(TransactionalClient client) {
		return this.clients.contains(client);
	}
	
	public void commitAll() throws StompException {
		for (TransactionalClient client : this.clients) {
			client.commit();
		}			
		// Let IllegalStateException through: It might point out an unexpectedly closed session.
	}

	public void closeAll() {
		for (TransactionalClient con : this.clients) {
			ConnectionFactoryUtils.releaseConnection(con);
		}
		this.clients.clear();
	}
}
