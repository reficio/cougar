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

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.*;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.StompConnectionFactory;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.AckType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ConnectionImpl;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-12
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQConnectionTest {

    private static BrokerService broker;
    private String stompQueuePrefix = "/queue/";
    private String destinationName = "request";

    @BeforeClass
    public static void initialize() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("stomp://localhost:61613");
        broker.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        broker.stop();
    }

    @Before
    public void setup() throws Exception {
        broker.getAdminView().addQueue(destinationName);
    }

    @After
    public void cleanup() throws Exception {
        broker.getAdminView().removeQueue(destinationName);
    }

    private StompConnectionFactory<Connection> getConnectionFactory() {
        StompConnectionFactory<Connection> factory = new StompConnectionFactory<Connection>(ConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");
        return factory;
    }

    @Test
    public void connect() {
        StompConnectionFactory<Connection> factory = getConnectionFactory();
        Connection conn = factory.createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void sendReceive() throws Exception {
        StompConnectionFactory<Connection> factory = getConnectionFactory();

        final String receiptId = UUID.randomUUID().toString();
        final String payload = "James Bond 007!";
        Connection connSender = factory.createConnection();
        connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.payload(payload);
                frame.receipt(receiptId);
            }
        });
        Frame receipt = connSender.receive();
        assertEquals(receiptId, receipt.receiptId());

        assertEquals(1, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        Connection connReceiver = factory.createConnection();
        connReceiver.subscribe(stompQueuePrefix + destinationName);
        Frame frame = connReceiver.receive();
        assertNotNull(frame);
        assertEquals(payload, frame.payload());
        assertEquals(stompQueuePrefix + destinationName, frame.destination());
        connReceiver.close();
        connSender.close();
    }

    @Test
    public void checkRedelivery() throws Exception {
        StompConnectionFactory<Connection> factory = getConnectionFactory();
        final String receiptId = UUID.randomUUID().toString();
        Connection connSender = factory.createConnection();
        String[] payloads = new String[]{"Jason Bourne" };//, "James Bond", "Eathen Hunt"};
        for(final String payload : payloads) {
            connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(payload);
                    frame.receipt(receiptId);
                }
            });
            Frame receipt = connSender.receive();
            assertEquals(receiptId, receipt.receiptId());
        }
        assertEquals(payloads.length, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        Connection connReceiver = factory.createConnection();
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.ack(AckType.CLIENT);
            }
        });
        Frame frame1 = connReceiver.receive();
        assertNotNull(frame1);
        // connReceiver.close();
        connReceiver.unsubscribe(subsId);
        frame1 = null;
        frame1 = connReceiver.receive();
        assertNotNull(frame1);

        assertEquals(1, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        Connection connReceiver2 = factory.createConnection();
        final String subsId2 = connReceiver2.subscribe(stompQueuePrefix + destinationName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.ack(AckType.CLIENT);
            }
        });
        Frame frame2 = connReceiver2.receive();
        assertNotNull(frame2);
        connReceiver2.close();
    }


    @Test
    public void checkAck() throws Exception {
        StompConnectionFactory<Connection> factory = getConnectionFactory();
        final String receiptId = UUID.randomUUID().toString();
        Connection connSender = factory.createConnection();
        String[] payloads = new String[]{"Jason Bourne" };//, "James Bond", "Eathen Hunt"};

        for(final String payload : payloads) {
            connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(payload);
                    frame.receipt(receiptId);
                }
            });
            Frame receipt = connSender.receive();
            assertEquals(receiptId, receipt.receiptId());
        }
        assertEquals(payloads.length, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);




        Connection connReceiver = factory.createConnection();
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.ack(AckType.CLIENT);
            }
        });
        Frame frame1 = connReceiver.receive();
        assertNotNull(frame1);




//        Frame frame2 = connReceiver.receive();
//        assertNotNull(frame2);

        final String receiptIdAck = UUID.randomUUID().toString();
        connReceiver.ack(frame1.messageId(), new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.receipt(receiptIdAck);
            }
        });
        Frame receiptAck = connReceiver.receive();
        while(receiptIdAck.equals(receiptAck.receiptId()) == false) {
            receiptAck = connReceiver.receive();
        }
        assertEquals(receiptIdAck, receiptAck.receiptId());


        final String receiptIdUns = UUID.randomUUID().toString();
        connReceiver.unsubscribe(subsId, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.receipt(receiptIdUns);
            }
        });
        Frame receiptUns = connReceiver.receive();
        while(receiptIdUns.equals(receiptUns.receiptId()) == false) {
            receiptUns = connReceiver.receive();
        }
        assertEquals(receiptIdUns, receiptUns.receiptId());


//        connReceiver.send(stompQueuePrefix + destinationName+"2", new FrameDecorator() {
//                @Override
//                public void decorateFrame(Frame frame) {
//                    frame.payload("TRELE");
//                }
//            });



        connReceiver.close();
        connSender.close();

        //assertEquals(payloads.length - 1, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
    }


}
