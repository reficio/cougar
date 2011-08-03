package org.reficio.stomp.connection;

import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.core.StompTransactionalResource;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 28/07/11
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TransactionalConnection extends StompTransactionalResource, StompResource, StompOperations {
}
