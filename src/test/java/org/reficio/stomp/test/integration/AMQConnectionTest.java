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
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ConnectionImpl;

import java.util.UUID;

import static org.junit.Assert.*;

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

    @Test
    public void connect() {
        StompConnectionFactory<Connection> factory = new StompConnectionFactory<Connection>(ConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");

        Connection conn = factory.createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void sendReceive() throws Exception {
        StompConnectionFactory<Connection> factory = new StompConnectionFactory<Connection>(ConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");

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

}
