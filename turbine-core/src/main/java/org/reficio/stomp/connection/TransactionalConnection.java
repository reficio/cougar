package org.reficio.stomp.connection;

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-07-28
 * Time: 3:59 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public interface TransactionalConnection extends StompResource, StompOperations {

    void begin() throws StompException;

    void rollback(FrameDecorator frameDecorator) throws StompException;

	void rollback() throws StompException;

	void commit(FrameDecorator frameDecorator) throws StompException;

	void commit() throws StompException;

}
