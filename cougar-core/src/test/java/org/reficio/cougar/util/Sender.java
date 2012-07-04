package org.reficio.cougar.util;

import org.reficio.cougar.connection.Client;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.impl.ConnectionBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/22/12
 * Time: 9:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class Sender implements Runnable {
    private final String hostname;
    private final int port;

    private int toSendCount;
    private String queueName;
    private int sent = 0;
    private Thread thread;

    public Client createConnection() {
        return ConnectionBuilder.connection().hostname(hostname).port(port).buildAndConnect();
    }

    public Sender(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute(String queueName, int toSendCount) {
        this.sent = 0;
        this.toSendCount = toSendCount;
        this.queueName = queueName;
        this.thread = new Thread(this);
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        Client connSender = createConnection();
        for (int i = 0; i < toSendCount; i++) {
            connSender.send(queueName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(Thread.currentThread().getName() + "\t" + (sent++));
                }
            });
        }
        connSender.close();
    }

    public int getSent() {
        return sent;
    }
}