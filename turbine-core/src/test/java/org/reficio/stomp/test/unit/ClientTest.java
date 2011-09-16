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
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.StompResourceState;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ClientImpl;
import org.reficio.stomp.test.mock.ClientStubImplMock;
import org.reficio.stomp.test.mock.IMockMessageHandler;
import org.reficio.stomp.test.mock.MockConnectionImpl;
import org.reficio.stomp.test.util.TestUtil;

import java.io.IOException;
import java.net.ServerSocket;
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
        connection = MockConnectionImpl.create();
        // register handlers
        connection.getStub().getServer().registerHandler(Command.CONNECT,
                new IMockMessageHandler() {
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
    }

    @After
    public void cleanup() {
        connection = null;
    }


    @Test
    public void connect() {
        // initialize the connection
        // connection.init("localhost", 61613, "user", "pass", "UTF-8");
        connection.hostname("localhost").port(61613).init();

        // test logic
        assertTrue(connection.isInitialized());
        connection.close();
        assertFalse(connection.isInitialized());
    }

    @Test
    public void connectNoSessionIdInResponse() {
        connection = MockConnectionImpl.create();
        // register handlers, no session id in response to connect command
        connection.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                return response;
            }
        });
        // initialize the connection
        // connection.init("localhost", 61613, "user", "pass", "UTF-8");
        connection.hostname("localhost").port(61613).init();
    }

    @Test(expected = StompProtocolException.class)
    public void connectHandshakeError() {
        // create connection object
        MockConnectionImpl conn = MockConnectionImpl.create();
        // register handlers
        conn.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.MESSAGE.getName(), false);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });
        // initialize the connection
        conn.hostname("localhost").port(61613).init();
    }

    @Test(expected = StompConnectionException.class)
    public void notInitializedError() {
        MockConnectionImpl connection = MockConnectionImpl.create();
        connection.send(new Frame(Command.MESSAGE.getName()));
    }

    @Test(expected = StompConnectionException.class)
    public void doubleInitError() {
        // initialize the connection
        connection.hostname("localhost").port(61613).init();
        connection.init();
        // connection.init("localhost", 61613, "user", "pass", "UTF-8");
        // connection.init("localhost", 61613, "user", "pass", "UTF-8");
    }

    @Test
    public void errorStateCheck() {
        // initialize the connection
        Exception e = null;
        connection.hostname("localhost").port(61613).init();
        // connection.init("localhost", 61613, "user", "pass", null);
        try {
            connection.hostname("localhost").port(61613).init();
            // connection.init("localhost", 61613, "user", "pass", null);
        } catch (Exception ex) {
            e = ex;
        } finally {
            assertNotNull(e);
            assertEquals(e.getClass(), StompConnectionException.class);
        }
        connection.send(new Frame(Command.ACK));
    }

    @Test(expected = StompConnectionException.class)
    public void errorStateCheckSecondInit() {
        // initialize the connection
        Exception e = null;
        // connection.init("localhost", 61613, "user", "pass", null);
        connection.hostname("localhost").port(61613).init();
        try {
            // connection.init("localhost", 61613, "user", "pass", null);
            connection.hostname("localhost").port(61613).init();
        } catch (Exception ex) {
            e = ex;
        } finally {
            assertNotNull(e);
        }
        // connection.init("localhost", 61613, "user", "pass", null);
        connection.hostname("localhost").port(61613).encoding(null).init();
    }


    @Test
    public void checkAttributes() {
        // initialize the connection
        // connection.init("localhost", 61613, "user", "pass", "UTF-8", 100);
        connection.hostname("localhost")
                .port(61613)
                .username("user")
                .password("pass")
                .timeout(100)
                .encoding("UTF-8")
                .init();
        assertEquals(connection.getHostname(), "localhost");
        assertEquals(connection.getPort(), 61613);
        assertEquals(connection.getUsername(), "user");
        assertEquals(connection.getPassword(), "pass");
        assertEquals(connection.getEncoding(), "UTF-8");
        assertEquals(connection.getTimeout(), 100);
        assertNotNull(connection.getSessionId());
    }

    @Test(expected = StompConnectionException.class)
    public void notMockedIOExceptionInSocketInit() {
        Client client = ClientImpl.create();
        client.hostname("localhost").port(TestUtil.getFreePort());
        client.init();
    }

    @Test(expected = StompConnectionException.class)
    public void notMockedIOExceptionInStreamsInit() {
        class MockClientImpl extends ClientImpl {
            protected void initializeCommunication(int timeout) {
                initializeSocket(timeout);
                // initializeStreams(timeout);
            }

            public void initializeStreamsPublic() {
                closeSocket();
                initializeStreams(1000);
            }

            @Override
            public synchronized void init() {
                assertNew();
                initializeCommunication(1000);
                setState(StompResourceState.COMMUNICATION_INITIALIZED);
                setState(StompResourceState.OPERATIONAL);
            }
        }

        ClientImpl client = new MockClientImpl();
        ServerSocket localmachine = null;
        int port = 0;
        try {
            localmachine = new ServerSocket(0);
            port = localmachine.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        client.hostname("localhost").port(port);
        MockClientImpl clientMock = ((MockClientImpl) client);
        clientMock.init();
        try {
            localmachine.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clientMock.initializeStreamsPublic();
    }

    @Test
    public void testInheritanceHierarchyAndFactoryMethodsAccessibility() {
        Client client = ClientImpl.create().hostname("localhost");
        client.port(123).password("123");
    }

    @Test(expected = NullPointerException.class)
    public void testParametersValidation() {
        ClientImpl.create().password(null);
    }

    @Test(expected = RuntimeException.class)
    public void testMarshallException() {
        ClientStubImplMock conn = new ClientStubImplMock();
        conn.init();
        conn.marshallPublic(new Frame(Command.SEND));
    }

    @Test(expected = RuntimeException.class)
    public void testUnMarshallException() {
        ClientStubImplMock conn = new ClientStubImplMock();
        conn.init();
        conn.unmarshallPublic();
    }

}
