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

package org.reficio.stomp.connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.IMockMessageHandler;
import org.reficio.stomp.impl.MockConnectionBuilder;
import org.reficio.stomp.impl.MockClientImpl;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-10
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class ConnectionTest {

    private MockClientImpl connection;

    @Before
    public void initialize() {
        connection = MockConnectionBuilder.mockConnection().build();
        // register handlers
        connection.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        connection.getStub().getServer().registerHandler(Command.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
        // initialize the connection
        // connection.init("localhost", 61613, "user", "pass", "UTF-8");
        connection.connect();
    }

    @After
    public void cleanup() {
        connection = null;
    }

    @Test
    public void begin() {
        connection.begin("tx1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.BEGIN);
        assertNotNull(frame);
        assertEquals(frame.transaction(), "tx1");
    }

    @Test
    public void commit() {
        connection.commit("tx1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.COMMIT);
        assertNotNull(frame);
        assertEquals(frame.transaction(), "tx1");
    }

    @Test
    public void abort() {
        connection.abort("tx1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.ABORT);
        assertNotNull(frame);
        assertEquals(frame.transaction(), "tx1");
    }

    @Test
    public void ack() {
        connection.ack("msg1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.ACK);
        assertNotNull(frame);
        assertEquals(frame.messageId(), "msg1");
    }

    @Test
    public void subscribe() {
        connection.subscribe("queue1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.SUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.destination(), "queue1");
    }

    @Test
    public void subscribeWithoutIdDecorator() {
        connection.subscribe("queue1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {

            }
        });
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.SUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.destination(), "queue1");
    }

    @Test
    public void subscribeWithIdInDecorator() {
        connection.subscribe("queue1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.subscriptionId("sub1");
            }
        });
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.SUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.destination(), "queue1");
        assertEquals(frame.subscriptionId(), "sub1");
    }

    @Test
    public void subscribeWithId() {
        connection.subscribe("sub1", "queue1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.SUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.destination(), "queue1");
        assertEquals(frame.subscriptionId(), "sub1");
    }

    @Test
    public void unsubscribe() {
        connection.unsubscribe("queue1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.UNSUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.subscriptionId(), "queue1");
    }

    @Test
    public void unsubscribeWithId() {
        connection.unsubscribe("sub1");
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.UNSUBSCRIBE);
        assertNotNull(frame);
        assertEquals(frame.subscriptionId(), "sub1");
    }

    @Test
    public void send() {
        connection.send("queue1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
            }
        });
        connection.close();
        Frame frame = connection.getServer().getLastFrameOfType(Command.SEND);
        assertNotNull(frame);
        assertEquals(frame.destination(), "queue1");
    }

    @Test(expected = StompEncodingException.class)
    public void testServerRejectsEncoding() {
        MockClientImpl conn = MockConnectionBuilder.mockConnection().build();
        // register handlers
        conn.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                response.session(UUID.randomUUID().toString());
                response.encoding("cp1252");
                return response;
            }
        });

        conn.connect();
    }

//    @Test
//    public void testInheritanceHierarchyAndFactoryMethodsAccessibility() {
//        Client connection = ConnectionImpl.create().hostname("localhost");
//        connection.port(123).password("123");
//    }


}



