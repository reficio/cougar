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

package org.reficio.stomp.test.unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompProtocolException;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.test.mock.IMockMessageHandler;
import org.reficio.stomp.test.mock.MockConnectionImpl;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-29
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ClientTest {

     private MockConnectionImpl connection;

    @Before
    public void initialize() {
        connection = new MockConnectionImpl();
        // register handlers
        connection.getStub().getServer().registerHandler(CommandType.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(CommandType.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        connection.getStub().getServer().registerHandler(CommandType.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(CommandType.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
    }

    @After
    public void cleanup() {
        connection = null;
    }


    @Test
    public void connect() {
        // initialize the connection
        connection.init("localhost", 61613, "user", "pass", "UTF-8");

        // test logic
        assertTrue(connection.isInitialized());
        connection.close();
        assertFalse(connection.isInitialized());
    }

    @Test
    public void connectNoSessionIdInResponse() {
        // initialize the connection
        connection.init("localhost", 61613, "user", "pass", "UTF-8");
        connection.close();
    }

    @Test(expected = StompProtocolException.class)
    public void connectHandshakeError() {
        // create connection object
        MockConnectionImpl conn = new MockConnectionImpl();
        // register handlers
        conn.getStub().getServer().registerHandler(CommandType.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(CommandType.MESSAGE);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });
        // initialize the connection
        conn.init("localhost", 61613, "user", "pass", "UTF-8");
    }

    @Test(expected = StompConnectionException.class)
    public void notInitializedError() {
        MockConnectionImpl connection = new MockConnectionImpl();
        connection.send(new Frame(CommandType.MESSAGE));
    }

    @Test(expected = StompConnectionException.class)
    public void doubleInitError() {
        // initialize the connection
        connection.init("localhost", 61613, "user", "pass", "UTF-8");
        connection.init("localhost", 61613, "user", "pass", "UTF-8");
    }

    @Test(expected = StompConnectionException.class)
    public void errorStateCheck() {
        // initialize the connection
        Exception e = null;
        connection.init("localhost", 61613, "user", "pass", null);
        try {
            connection.init("localhost", 61613, "user", "pass", null);
        } catch(Exception ex) {
            e = ex;
        } finally {
            assertNotNull(e);
        }
        connection.send(new Frame(CommandType.ACK));
    }

    @Test(expected = StompConnectionException.class)
    public void errorStateCheckSecondInit() {
        // initialize the connection
        Exception e = null;
        connection.init("localhost", 61613, "user", "pass", null);
        try {
            connection.init("localhost", 61613, "user", "pass", null);
        } catch(Exception ex) {
            e = ex;
        } finally {
            assertNotNull(e);
        }
        connection.init("localhost", 61613, "user", "pass", null);
    }


    @Test
    public void checkAttributes() {
        // initialize the connection
        connection.init("localhost", 61613, "user", "pass", "UTF-8", 100);
        assertEquals(connection.getHostname(), "localhost");
        assertEquals(connection.getPort(), 61613);
        assertEquals(connection.getUsername(), "user");
        assertEquals(connection.getPassword(), "pass");
        assertEquals(connection.getEncoding(), "UTF-8");
        assertNotNull(connection.getSessionId());


    }

}
