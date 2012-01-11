/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.reficio.stomp.test.integration;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.junit.Test;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Ack;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.Turbine;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-12
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQConnectionTest extends AbstractAMQIntegrationTest<Connection> {

    public Connection createConnection() {
        return Turbine.connection().hostname(HOSTNAME).port(PORT).buildAndInit();
    }

    @Test
    public void connect() {
        Connection conn = createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void singleSendReceive() throws Exception {

        final String receiptId = UUID.randomUUID().toString();
        final String payload = "James Bond 007!";
        Connection connSender = createConnection();
        connSender.send(getQueueName(), new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.payload(payload);
                frame.receipt(receiptId);
            }
        });
        Frame receipt = connSender.receive();
        assertEquals(receiptId, receipt.receiptId());

        assertEquals(1, getQueueLength());

        Connection connReceiver = createConnection();
        connReceiver.subscribe(getQueueName());
        Frame frame = connReceiver.receive();
        assertNotNull(frame);
        assertEquals(payload, frame.payload());
        assertEquals(getQueueName(), frame.destination());
        connReceiver.close();
        connSender.close();
    }

    @Test
    public void consecutiveSendReceive() throws Exception {
        final int NUMBER_OF_MSGS = 100;
        Connection connSender = createConnection();
        for (int i = 0; i < NUMBER_OF_MSGS; i++) {
            connSender.send(getQueueName(), new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(System.currentTimeMillis() + "");
                }
            });
        }
        connSender.close();

        Connection connReceiver = createConnection();
        String subId = connReceiver.subscribe(getQueueName());
        for (int i = 0; i < NUMBER_OF_MSGS; i++) {
            assertNotNull(connReceiver.receive());
        }
        connReceiver.unsubscribe(subId);
        connReceiver.close();
        connReceiver = null;
    }

    class Sender implements Runnable {
        private int toSendCount;
        private String queueName;
        private int sent = 0;

        public Sender(int toSendCount, String queueName) {
            this.toSendCount = toSendCount;
            this.queueName = queueName;
        }

        @Override
        public void run() {
            Connection connSender = createConnection();
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

    class Receiver implements Runnable {
        private int toReceiveCount;
        private String queueName;
        private int received = 0;
        private AtomicInteger counter;

        public Receiver(AtomicInteger counter, int toReceiveCount, String queueName) {
            this.toReceiveCount = toReceiveCount;
            this.queueName = queueName;
            this.counter = counter;
        }

        @Override
        public void run() {
            Connection connReceiver = createConnection();
            String subId = connReceiver.subscribe(getQueueName(), new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.custom("activemq.prefetchSize", "1");
                }
            });
            while (true) {
                Frame rcv = connReceiver.receive(250);
                if (rcv != null) {
                    received++;
                }
                if (counter.addAndGet(1) >= toReceiveCount) {
                    break;
                }
            }
            connReceiver.unsubscribe(subId);
            Frame afterUnsubscribe = connReceiver.receive(1000);
            if (afterUnsubscribe != null) {
                counter.incrementAndGet();
                received++;
            }
            connReceiver.close();
            connReceiver = null;
        }

        public int getReceived() {
            return received;
        }
    }

    class DisconnectingReceiver implements Runnable {
        private int toReceiveCount;
        private String queueName;
        private int received = 0;
        private AtomicInteger counter;
        private boolean autoAck;

        public DisconnectingReceiver(boolean autoAck, AtomicInteger counter, int toReceiveCount, String queueName) {
            this.toReceiveCount = toReceiveCount;
            this.queueName = queueName;
            this.counter = counter;
            this.autoAck = autoAck;
        }

        @Override
        public void run() {
            while (true) {
                if (counter.get() >= toReceiveCount) {
                    break;
                }
                Connection connReceiver = createConnection();
                String subId = connReceiver.subscribe(getQueueName(), new FrameDecorator() {
                    @Override
                    public void decorateFrame(Frame frame) {
                        frame.custom("activemq.prefetchSize", "1");
                        if(!autoAck) {
                            frame.ack(Ack.CLIENT);
                        }
                    }
                });

                Frame rcv = connReceiver.receive(250);
                if (rcv != null) {
                    received++;
                    counter.incrementAndGet();
                    if(!autoAck) {
                        connReceiver.ack(rcv.messageId());
                    }
                }
                connReceiver.unsubscribe(subId);

                for (int i = 0; i < 10; i++) {
                    Frame afterUnsubscribe = connReceiver.receive(300);
                    if (afterUnsubscribe != null && afterUnsubscribe.getCommand().equals(Command.MESSAGE)) {
                        received++;
                        counter.incrementAndGet();
                        if(!autoAck)
                            connReceiver.ack(afterUnsubscribe.messageId());
                    }
//                    else {
//                        break;
//                    }
                }

                connReceiver.close();
                connReceiver = null;
            }
        }

        public int getReceived() {
            return received;
        }
    }

    @Test
    public void parallelSendReceive() throws Exception {
        String queue = getQueueName();
        AtomicInteger counter = new AtomicInteger(0);


        Sender sender1 = new Sender(100, queue);
        Sender sender2 = new Sender(100, queue);

        Receiver receiver1 = new Receiver(counter, 200, queue);
        Receiver receiver2 = new Receiver(counter, 200, queue);

        Thread t1 = new Thread(sender1);
        Thread t2 = new Thread(sender2);

        Thread t3 = new Thread(receiver1);
        Thread t4 = new Thread(receiver2);

        t3.start();
        t4.start();
        t1.start();
        t2.start();

        t3.join();
        t4.join();
        t1.join();
        t2.join();

        assertEquals(sender1.getSent() + sender2.getSent(), receiver1.getReceived() + receiver2.getReceived());


    }

    @Test
    public void prefetchSize() throws Exception {

        StompConnection connection;
        String queue = getQueueName() + System.currentTimeMillis();




        Sender sender1 = new Sender(1000, queue);
        Thread t1 = new Thread(sender1);
        t1.start();
        t1.join();

        connection = new StompConnection();
        connection.open("localhost", 61613);
        connection.connect("user", "password");
        System.out.println("Connected");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Stomp.Headers.Send.PERSISTENT, "true");
        map.put("activemq.prefetchSize", "1");
        map.put("activemq.dispatchAsync", "false");
        connection.subscribe(queue, Stomp.Headers.Subscribe.AckModeValues.AUTO, map);

        StompFrame frame = connection.receive();
        if(frame != null) {
            System.out.println("Message received");
        }
        connection.unsubscribe(queue);
        System.out.println("Unsubscribed");

        Thread.sleep(1000);


        int received = 1;

        for (int i = 0; i < 1001; i++) {
            StompFrame afterUnsubscribe = connection.receive(1500);
            if (afterUnsubscribe != null) {
                // System.out.println("Message received, after unsubscribe");
                // assertTrue(false);
                received++;
                System.out.println(received);
            }
        }

        connection.disconnect();
        System.out.println("Disconnected");

    }

    @Test
    public void parallelSendReceiveDisconnectingAutoAck() throws Exception {
        String queue = getQueueName();
        AtomicInteger counter = new AtomicInteger(0);

        assertEquals(0, getQueueLength());


        Sender sender1 = new Sender(4, queue);
        Sender sender2 = new Sender(4, queue);

        DisconnectingReceiver receiver1 = new DisconnectingReceiver(false, counter, 8, queue);
        DisconnectingReceiver receiver2 = new DisconnectingReceiver(false, counter, 8, queue);

        Thread t1 = new Thread(sender1);
        Thread t2 = new Thread(sender2);

        Thread t3 = new Thread(receiver1);
        Thread t4 = new Thread(receiver2);

        t3.start();
        t4.start();
        t1.start();
        t2.start();

        t3.join();
        t4.join();
        t1.join();
        t2.join();


        int enqueued = sender1.getSent() + sender2.getSent();
        int inQueue = getQueueLength();
        int dequeued = receiver1.getReceived() + receiver2.getReceived();



        assertEquals(enqueued, inQueue + dequeued);
        System.out.println(inQueue);


    }


    @Test
    public void parallelSendReceiveDisconnectingManualAck() throws Exception {
        String queue = getQueueName();
        AtomicInteger counter = new AtomicInteger(0);

        assertEquals(0, getQueueLength());


        Sender sender1 = new Sender(4, queue);
        Sender sender2 = new Sender(4, queue);

        DisconnectingReceiver receiver1 = new DisconnectingReceiver(true, counter, 8, queue);
        DisconnectingReceiver receiver2 = new DisconnectingReceiver(true, counter, 8, queue);

        Thread t1 = new Thread(sender1);
        Thread t2 = new Thread(sender2);

        Thread t3 = new Thread(receiver1);
        Thread t4 = new Thread(receiver2);

        t3.start();
        t4.start();
        t1.start();
        t2.start();

        t3.join();
        t4.join();
        t1.join();
        t2.join();

        int enqueued = sender1.getSent() + sender2.getSent();
        int inQueue = getQueueLength();
        int dequeued = receiver1.getReceived() + receiver2.getReceived();

        assertEquals(enqueued, inQueue + dequeued);
        System.out.println(inQueue);
    }



}
