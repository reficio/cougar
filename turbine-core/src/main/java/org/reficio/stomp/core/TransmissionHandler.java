package org.reficio.stomp.core;

import org.reficio.stomp.*;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;


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
