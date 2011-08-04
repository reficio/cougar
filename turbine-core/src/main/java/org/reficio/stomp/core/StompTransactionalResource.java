package org.reficio.stomp.core;

import org.reficio.stomp.StompException;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 28/07/11
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface StompTransactionalResource {

    void begin() throws StompException;

    void rollback(FrameDecorator frameDecorator) throws StompException;

	void rollback() throws StompException;

	void commit(FrameDecorator frameDecorator) throws StompException;

	void commit() throws StompException;


    StompResource autoTransactional(boolean autoTransactional);
    StompResource autoAcknowledge(boolean autoAcknowledge);


    boolean isAutoTransactional();
    boolean isAutoAcknowledge();


}
