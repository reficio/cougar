package org.reficio.stomp.spring.core;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.TxConnection;

public interface ConnectionCallback<T> {

	T doInStomp(TxConnection connection) throws StompException;

}
