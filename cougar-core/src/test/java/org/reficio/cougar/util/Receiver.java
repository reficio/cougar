package org.reficio.cougar.util;

import org.reficio.cougar.StompSocketTimeoutException;
import org.reficio.cougar.connection.Client;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.domain.Ack;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.impl.ConnectionBuilder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/22/12
 * Time: 9:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class Receiver implements Runnable {
    private final String hostname;
    private final int port;
    private int toReceiveCount;
    private String queueName;
    private int received = 0;
    private AtomicInteger counter;
    private Thread thread;

    public Client createConnection() {
        return ConnectionBuilder.connection().hostname(hostname).port(port).buildAndConnect();
    }

    public Receiver(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute(AtomicInteger counter, String queueName, int toReceiveCount) {
        this.toReceiveCount = toReceiveCount;
        this.queueName = queueName;
        this.counter = counter;
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
        Client connReceiver = createConnection();
        String subId = connReceiver.subscribe(queueName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.custom("activemq.prefetchSize", "1");
                frame.ack(Ack.CLIENT_INDIVIDUAL);
            }
        });
        while (counter.get() < toReceiveCount) {
            try {
                Frame rcv = connReceiver.receive(250);
                if (rcv != null) {
                    connReceiver.ack(rcv.messageId());
                    counter.incrementAndGet();
                    received++;
                }
            } catch (StompSocketTimeoutException ex) {
                // ignore
            }
        }

        connReceiver.unsubscribe(subId);
        // only one receive after since the prefetch size is set 1
        try {
            Frame afterUnsubscribe = connReceiver.receive(1000);
            if (afterUnsubscribe != null) {
                counter.incrementAndGet();
                received++;
            }
        } catch (StompSocketTimeoutException ex) {
            // ignore
        }

        connReceiver.close();
        connReceiver = null;
    }

    public int getReceived() {
        return received;
    }
}