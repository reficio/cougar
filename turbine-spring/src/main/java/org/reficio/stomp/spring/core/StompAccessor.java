package org.reficio.stomp.spring.core;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.spring.connection.StompResourceHolder;
import org.reficio.stomp.connection.TxConnection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class StompAccessor implements InitializingBean {

	private volatile ConnectionFactory<TxConnection> connectionFactory;
	
	protected abstract <T> T execute(ConnectionCallback<T> action) throws StompException;
	
	public void setConnectionFactory(ConnectionFactory<TxConnection> connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory<TxConnection> getConnectionFactory() {
		return this.connectionFactory;
	}

	public void afterPropertiesSet() {
		Assert.notNull(this.connectionFactory, "ConnectionFactory is required");
	}

	protected TxConnection getConnection(StompResourceHolder holder) {
		return holder.getConnection();
	}
	
}
