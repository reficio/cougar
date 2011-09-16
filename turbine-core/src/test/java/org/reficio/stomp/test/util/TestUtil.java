package org.reficio.stomp.test.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 29/08/11
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestUtil {

    public static int getFreePort() {
        try {
            ServerSocket localmachine = new ServerSocket(0);
            localmachine.close();
            int port = localmachine.getLocalPort();
            return port;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
