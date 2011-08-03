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
import org.junit.*;
import org.reficio.stomp.connection.StompConnectionFactory;
import org.reficio.stomp.core.StompTransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.StompTxConnectionImpl;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-13
 * Time: 10:30 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQTxConnectionTest {

    private static BrokerService broker;
    private String stompQueuePrefix = "";//"/queue/";
    private String destinationName = "jms.queue.request";

//    @BeforeClass
//    public static void initialize() throws Exception {
//        broker = new BrokerService();
//        broker.setPersistent(false);
//        broker.addConnector("stomp://localhost:61613");
//        broker.start();
//    }
//
//    @AfterClass
//    public static void stop() throws Exception {
//        broker.stop();
//    }

    private StompConnectionFactory<StompTransactionalConnection> getConnectionFactory() {
        StompConnectionFactory<StompTransactionalConnection> factory = new StompConnectionFactory<StompTransactionalConnection>(StompTxConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }


//    @Before
//    public void setup() throws Exception {
//        broker.getAdminView().addQueue(destinationName);
//    }
//
//    @After
//    public void cleanup() throws Exception {
//        broker.getAdminView().removeQueue(destinationName);
//    }

    @Test
    public void connect() {
        StompConnectionFactory<StompTransactionalConnection> factory = new StompConnectionFactory<StompTransactionalConnection>(StompTxConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");

        StompTransactionalConnection conn = factory.createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void checkRedelivery() throws Exception {
        StompConnectionFactory<StompTransactionalConnection> factory = getConnectionFactory();
        final String receiptId = UUID.randomUUID().toString();
        StompTransactionalConnection connSender = factory.createConnection();
        String[] payloads = new String[]{"Jason Bourne"};//, "James Bond", "Eathen Hunt"};
        for (final String payload : payloads) {
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
        connSender.commit();
        connSender.close();
        // assertEquals(payloads.length, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        StompTransactionalConnection connReceiver = factory.createConnection();
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName);
        Frame frame1 = connReceiver.receive();
        assertNotNull(frame1);
        connReceiver.ack(frame1.messageId());
        connReceiver.rollback();
        connReceiver.close();

        // assertEquals(1, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        StompTransactionalConnection connReceiver2 = factory.createConnection();
        final String subsId2 = connReceiver2.subscribe(stompQueuePrefix + destinationName);
        Frame frame2 = connReceiver2.receive();
        assertNotNull(frame2);
        connReceiver2.close();
    }


    @Test
    public void checkRollback() throws Exception {

        StompConnectionFactory<StompTransactionalConnection> factory = getConnectionFactory();
        final String receiptId = UUID.randomUUID().toString();
        StompTransactionalConnection connSender = factory.createConnection();
        String[] payloads = new String[]{"Jason Bourne", "James Bond", "Eathen Hunt"};
        for (final String payload : payloads) {
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
        connSender.commit();
        connSender.close();
        // assertEquals(payloads.length, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        StompTransactionalConnection connReceiver = factory.createConnection();
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName);
        Frame frame1 = connReceiver.receive();
        Frame frame2 = connReceiver.receive();
        Frame frame3 = connReceiver.receive();
        assertNotNull(frame1);
        assertNotNull(frame2);
        assertNotNull(frame3);
        connReceiver.rollback();
        connReceiver.close();


//        assertEquals(payloads.length, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
//        Frame frame4 = connReceiver.receive();
//        assertNotNull(frame4);

        StompTransactionalConnection connReceiver2 = factory.createConnection();
        final String subsId2 = connReceiver2.subscribe(stompQueuePrefix + destinationName);
        Frame framee1 = connReceiver2.receive();
        Frame framee2 = connReceiver2.receive();
        Frame framee3 = connReceiver2.receive();
        assertNotNull(framee1);
        assertNotNull(framee2);
        assertNotNull(framee3);
        connReceiver2.commit();

        // assertEquals(0, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
//        connReceiver.close();
        connReceiver2.close();
    }


}