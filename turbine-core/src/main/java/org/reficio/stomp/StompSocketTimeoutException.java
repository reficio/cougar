package org.reficio.stomp;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 19/09/11
 * Time: 8:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class StompSocketTimeoutException extends RuntimeException {

    public StompSocketTimeoutException(Throwable cause) {
        super(cause);
    }

    public StompSocketTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
