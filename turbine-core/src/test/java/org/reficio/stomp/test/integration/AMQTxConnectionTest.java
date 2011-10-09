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
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ConnectionImpl;
import org.reficio.stomp.impl.StompConnectionFactory;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.impl.TransactionalConnectionImpl;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

    private StompConnectionFactory<TransactionalConnection> getConnectionFactory() {
        StompConnectionFactory<TransactionalConnection> factory = new StompConnectionFactory<TransactionalConnection>(TransactionalConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");
        return factory;
    }

    @Test
    public void connect() {
        TransactionalConnection conn = getConnectionFactory().createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void sendRollback() throws Exception {
        assertEquals(0, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        final int NUMBER_OF_MSGS = 100;
        TransactionalConnection connSender = getConnectionFactory().createConnection();
        connSender.begin();
        for (int i = 0; i < NUMBER_OF_MSGS; i++)
            connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(System.currentTimeMillis() + "");
                }
            });
        assertEquals(0, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);

        connSender.rollback();
        connSender.close();

        assertEquals(0, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
    }

}