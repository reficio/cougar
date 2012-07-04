package org.reficio.cougar.core;

import org.reficio.cougar.domain.Frame;


/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 09/10/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TransmissionHandler {
    void initializeCommunication(int timeout);
    void closeCommunication();
    void setReceptionTimeoutInMillis(int timeout);
    void marshall(Frame frame);
    Frame unmarshall();
}
