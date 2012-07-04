package org.reficio.cougar.perf.sender;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.DefaultMessage;
import pk.aamir.stompj.Message;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class StompJSender implements ISender {

    Connection conn;

    @Override
    public void initialize(String hostname, int port, String username, String password, String encoding) throws Exception {
        conn = new Connection(hostname, port, username, password);
        conn.connect();
    }

    @Override
    public void close() throws Exception {
        conn.disconnect();
    }

    @Override
    public void send(String queue, String payload) throws Exception {
        DefaultMessage msga = new DefaultMessage();
        msga.setProperty("persistent", "false");
        msga.setContent(payload);
        // msg.setContentAsString("Another test message!");
        conn.send(msga, queue);
    }

    @Override
    public void send(String queue, String receiptId, String payload) throws Exception {
//        Message msga = new DefaultMessage();
//        msga.setProperty("receipt", receiptId);
//        // msg.setContentAsString("Another test message!");
//        conn.send(msga, payload);
//        conn.
    }

    @Override
    public String receiveReceipt() throws Exception {
        //conn.removeMessageHandlers()
        return null;
    }
}
