package org.reficio.stomp.perf.sender;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class ActiveMQSender implements ISender {

    private StompConnection connection;

    @Override
    public void initialize(String hostname, int port, String username, String password, String encoding) throws Exception {
        connection = new StompConnection();
        connection.open(hostname, port);
        connection.connect(username, password);
    }

    @Override
    public void close() throws Exception {
        connection.disconnect();
    }

    @Override
    public void send(String queue, String payload) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Stomp.Headers.Send.PERSISTENT, "false");
        connection.send(queue, payload, null, map);
    }

    @Override
    public void send(String queue, String receiptId, String payload) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Stomp.Headers.RECEIPT_REQUESTED, receiptId);
        map.put(Stomp.Headers.Send.PERSISTENT, "false");
        connection.send(queue, payload, null, map);
    }

    @Override
    public String receiveReceipt() throws Exception {
        StompFrame frame = connection.receive();
        return frame.getHeaders().get(Stomp.Headers.Response.RECEIPT_ID);
    }

}
