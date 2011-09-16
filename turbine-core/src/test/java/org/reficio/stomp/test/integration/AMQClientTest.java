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
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.impl.StompConnectionFactory;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ClientImpl;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-13
 * Time: 10:30 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQClientTest {
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
    public static void stop() throws Exception {
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

    private StompConnectionFactory<Client> getConnectionFactory() {
        StompConnectionFactory<Client> factory = new StompConnectionFactory<Client>(ClientImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");
        return factory;
    }

    @Test
    public void connect() {
        StompConnectionFactory<Client> factory = getConnectionFactory();
        Client client = factory.createConnection();
        assertTrue(client.isInitialized());
        client.close();
        assertFalse(client.isInitialized());
    }

    @Test
    public void connectNotUTF() {
        Client client = ClientImpl.create();
        client.hostname("localhost").port(61613).encoding("cp1252").init();

        assertTrue(client.isInitialized());
        client.close();
        assertFalse(client.isInitialized());
    }

    @Test
    public void send() throws Exception {
        StompConnectionFactory<Client> factory = getConnectionFactory();
        Client client = factory.createConnection();

        final String receiptSubscribe = UUID.randomUUID().toString();
        Frame frameSubscribe = new Frame(Command.SUBSCRIBE);
        frameSubscribe.destination(stompQueuePrefix + destinationName);
        frameSubscribe.receipt(receiptSubscribe);
        client.send(frameSubscribe);
        Frame responseSubscribe = client.receive();
        assertNotNull(responseSubscribe);

        final String payload = "TEST MESSAGE";
        final String receiptId = UUID.randomUUID().toString();
        Frame frame = new Frame(Command.SEND);
        frame.destination(stompQueuePrefix + destinationName);
        frame.payload(payload);
        frame.receipt(receiptId);
        client.send(frame);
        Frame receipt = client.receive();
        assertTrue(receipt.getCommand().equals(Command.RECEIPT));

        Frame receivedFrame = client.receive();
        assertNotNull(receivedFrame);
        assertEquals(payload, receivedFrame.payload());
        assertEquals(stompQueuePrefix + destinationName, receivedFrame.destination());

        client.close();
    }

}
