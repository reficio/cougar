package org.reficio.cougar;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class StompSocketTimeoutException extends StompException {

    private static final long serialVersionUID = 1L;

    public StompSocketTimeoutException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public StompSocketTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

	public StompSocketTimeoutException(String message) {
		super(message);
	}

}
