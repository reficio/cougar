package org.reficio.cougar.perf.sender;

import org.reficio.cougar.connection.Connection;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.factory.SimpleConnectionFactory;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class TurbineSender implements ISender {

    private Connection connection;

    @Override
    public void initialize(String hostname, int port, String username, String password, String encoding) {
        SimpleConnectionFactory<Connection> factory = new SimpleConnectionFactory<Connection>(Connection.class);
        factory.setEncoding(encoding);
        factory.setHostname(hostname);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        connection = factory.createConnection();
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public void send(String queue, String payload) {
        Frame frame = new Frame(Command.SEND);
        frame.destination(queue);
        frame.payload(payload);
        frame.custom("persistent", "false");
        connection.send(frame);
    }

    @Override
    public void send(String queue, String receiptId, String payload) throws Exception {
        Frame frame = new Frame(Command.SEND);
        frame.destination(queue);
        frame.receipt(receiptId);
        frame.custom("persistent", "false");
        frame.payload(payload);
        connection.send(frame);
    }

    @Override
    public String receiveReceipt() throws Exception {
        Frame frame = connection.receive();
        return frame.receiptId();
    }

}
