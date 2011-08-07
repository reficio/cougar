package org.reficio.stomp.connection;

import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.core.StompTransactionalResource;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-07-28
 * Time: 3:59 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public interface TransactionalConnection extends StompTransactionalResource, StompResource, StompOperations {
}
