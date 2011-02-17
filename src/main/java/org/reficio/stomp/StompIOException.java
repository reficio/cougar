package org.reficio.stomp;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-17
 * Time: 9:45 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class StompIOException extends StompException {

	private static final long serialVersionUID = 1L;

	public StompIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public StompIOException(String message) {
		super(message);
	}

}
