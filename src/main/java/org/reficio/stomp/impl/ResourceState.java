package org.reficio.stomp.impl;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-17
 * Time: 10:53 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public enum ResourceState {
    NEW,
    COMMUNICATION_INITIALIZED,
    OPERATIONAL,
    ERROR,
    CLOSING,
    CLOSED
}
