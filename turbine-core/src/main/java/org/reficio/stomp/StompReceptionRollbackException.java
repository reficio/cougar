package org.reficio.stomp;

import org.reficio.stomp.domain.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 27/07/11
 * Time: 9:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class StompReceptionRollbackException extends StompException {

	private static final long serialVersionUID = 1L;

    private List<Frame> notRolledBackMessages = new ArrayList<Frame>();

	public StompReceptionRollbackException(String message, Throwable cause, List<Frame> notRolledBackMessages) {
		super(message, cause);
        this.notRolledBackMessages = notRolledBackMessages;
	}

	public StompReceptionRollbackException(String message, List<Frame> notRolledBackMessages) {
		super(message);
        this.notRolledBackMessages = notRolledBackMessages;
	}

}