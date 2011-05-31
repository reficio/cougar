package org.reficio.stomp.spring.connection;

import org.reficio.stomp.connection.TxConnection;

public interface TxConnectionProxy extends TxConnection {

	/**
	 * Return the target Session of this proxy.
	 * <p>
	 * This will typically be the native provider Session or a wrapper from a
	 * session pool.
	 * 
	 * @return the underlying Session (never <code>null</code>)
	 */
	TxConnection getTargetConnection();

}