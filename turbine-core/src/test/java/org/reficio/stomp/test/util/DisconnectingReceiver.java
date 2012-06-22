package org.reficio.stomp.test.util;

import org.reficio.stomp.StompSocketTimeoutException;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Ack;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ConnectionBuilder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/22/12
 * Time: 9:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class DisconnectingReceiver implements Runnable {
    private final String hostname;
    private final int port;
    private int toReceiveCount;
    private String queueName;
    private int received = 0;
    private AtomicInteger counter;
    private boolean autoAck;
    private Thread thread;

    public DisconnectingReceiver(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute(AtomicInteger counter, String queueName, int toReceiveCount, boolean autoAck) {
        this.toReceiveCount = toReceiveCount;
        this.queueName = queueName;
        this.counter = counter;
        this.autoAck = autoAck;
        this.thread = new Thread(this);
        thread.start();
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    public Connection createConnection() {
        return ConnectionBuilder.connection().hostname(hostname).port(port).buildAndConnect();
    }

    @Override
    public void run() {
        while (counter.get() < toReceiveCount) {
            Connection connReceiver = createConnection();
            String subId = connReceiver.subscribe(queueName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.custom("activemq.prefetchSize", "1");
                    if (!autoAck) {
                        frame.ack(Ack.CLIENT);
                    }
                }
            });

            try {
                Frame rcv = connReceiver.receive(250);
                if (rcv != null) {
                    received++;
                    counter.incrementAndGet();
                    if (!autoAck) {
                        connReceiver.ack(rcv.messageId());
                    }
                }
            } catch (StompSocketTimeoutException ex) {
                // ignore
            }

            connReceiver.unsubscribe(subId);
            connReceiver.close();
            connReceiver = null;
        }
    }

    public int getReceived() {
        return received;
    }
}
