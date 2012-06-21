package org.reficio.stomp;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class StompSocketTimeoutException extends RuntimeException {

    public StompSocketTimeoutException(Throwable cause) {
        super(cause);
    }

    public StompSocketTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

}
