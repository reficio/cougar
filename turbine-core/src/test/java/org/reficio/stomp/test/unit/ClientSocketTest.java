package org.reficio.stomp.test.unit;

import org.junit.Test;
import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.impl.ClientImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class ClientSocketTest {

    public class TestClientImpl extends ClientImpl {
        protected void connect() {
        }
    }

    @Test
    public void connectAndClose() throws IOException {
        ServerSocket srv = null;
        srv = new ServerSocket(32611);
        Client client = new TestClientImpl();
        client.init("localhost", 32611, "user", "pass", "UTF-8");
        Socket comm = srv.accept();
        assertNotNull(comm);
        client.close();
        srv.close();
    }

    @Test(expected = StompEncodingException.class)
    public void unsupportedEncoding() throws IOException {
        ServerSocket srv = null;
        srv = new ServerSocket(32611);
        Client client = new TestClientImpl();
        client.init("localhost", 32611, "user", "pass", "NO_SUCH_ENCODING");
    }

    @Test(expected = StompConnectionException.class)
    public void connectionError() throws IOException {
        Client client = new TestClientImpl();
        client.init("localhost", 32612, "user", "pass", "UTF-8");
    }

    @Test
    public void receive() throws IOException {
//        ServerSocket srv = null;
//        srv = new ServerSocket(32611);
//        Client client = new TestClientImpl();
//        client.init("localhost", 32611, "user", "pass", "UTF-8");
//        srv.close();
//        client.close();
    }

}
