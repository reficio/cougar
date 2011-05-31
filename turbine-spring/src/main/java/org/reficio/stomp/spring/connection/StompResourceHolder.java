package org.reficio.stomp.spring.connection;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.TxConnection;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class StompResourceHolder extends ResourceHolderSupport {

	private static final Log logger = LogFactory.getLog(StompResourceHolder.class);

	private boolean frozen = false;

	private final List<TxConnection> connections = new LinkedList<TxConnection>();

	public StompResourceHolder() {
	}

	public StompResourceHolder(TxConnection connection) {
		addConnection(connection);
	}

	public final boolean isFrozen() {
		return this.frozen;
	}

	public final void addConnection(TxConnection connection) {
		Assert.isTrue(!this.frozen, "Cannot add Connection because StompResourceHolder is frozen");
		Assert.notNull(connection, "Connection must not be null");
		if (!this.connections.contains(connection)) {
			this.connections.add(connection);
		}
	}

	public TxConnection getConnection() {
		return (!this.connections.isEmpty() ? this.connections.get(0) : null);
	}

	public TxConnection getConnection(Class<? extends TxConnection> connectionType) {
		return CollectionUtils.findValueOfType(this.connections, connectionType);
	}

	public boolean containsConnection(TxConnection connection) {
		return this.connections.contains(connection);
	}
	
	public void commitAll() throws StompException {
		for (TxConnection connection : this.connections) {
			connection.commit();
		}			
		// Let IllegalStateException through: It might point out an unexpectedly closed session.
	}

	public void closeAll() {
		for (TxConnection con : this.connections) {
			ConnectionFactoryUtils.releaseConnection(con);
		}
		this.connections.clear();
	}
}
