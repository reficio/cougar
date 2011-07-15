package org.reficio.stomp.perf.sender;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompFrame;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.StompConnectionFactory;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ClientImpl;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class TurbineSender implements ISender {

    private Client client;

    @Override
    public void initialize(String hostname, int port, String username, String password, String encoding) {
        StompConnectionFactory<Client> factory = new StompConnectionFactory<Client>(ClientImpl.class);
        factory.setEncoding(encoding);
        factory.setHostname(hostname);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        client = factory.createConnection();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void send(String queue, String payload) {
        Frame frame = new Frame(CommandType.SEND);
        frame.destination(queue);
        frame.payload(payload);
        frame.custom("persistent", "false");
        client.send(frame);
    }

    @Override
    public void send(String queue, String receiptId, String payload) throws Exception {
        Frame frame = new Frame(CommandType.SEND);
        frame.destination(queue);
        frame.receipt(receiptId);
        frame.custom("persistent", "false");
        frame.payload(payload);
        client.send(frame);
    }

    @Override
    public String receiveReceipt() throws Exception {
        Frame frame = client.receive();
        return frame.receiptId();
    }

}
