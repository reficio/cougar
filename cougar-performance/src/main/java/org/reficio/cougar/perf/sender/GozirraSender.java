package org.reficio.cougar.perf.sender;

import net.ser1.stomp.Client;

import java.util.HashMap;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class GozirraSender implements ISender {

    Client client;
    String receiptId;

    @Override
    public void initialize(String hostname, int port, String username, String password, String encoding) throws Exception {
        client = new Client(hostname, port, username, password);
    }

    @Override
    public void close() throws Exception {
        client.disconnect();
    }

    @Override
    public void send(String queue, String payload) throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("persistent", "false");
        client.send(queue, payload, headers);
    }

    @Override
    public void send(String queue, String receiptId, String payload) throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("persistent", "false");
        headers.put("receipt", receiptId);

        this.receiptId = receiptId;
        // client.sendW(queue, payload, headers);
        client.send(queue, payload, headers);
    }

    @Override
    public String receiveReceipt() throws Exception {
        if (client.waitOnReceipt(receiptId, 20000)) {
            return receiptId;
        } else {
            return "";
        }
    }
}
