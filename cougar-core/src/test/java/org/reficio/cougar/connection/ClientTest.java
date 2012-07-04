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

package org.reficio.cougar.connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reficio.cougar.StompConnectionException;
import org.reficio.cougar.StompProtocolException;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.impl.IMockMessageHandler;
import org.reficio.cougar.impl.MockConnectionImpl;
import org.reficio.cougar.impl.MockConnectionBuilder;

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

    private MockConnectionImpl client;

    @Before
    public void initialize() {

        client = MockConnectionBuilder.mockClient()
                .hostname("localhost")
                .port(61613)
                .username("user")
                .password("pass")
                .timeout(100)
                .encoding("UTF-8")
                .build();

        // register handlers
        client.getStub().getServer().registerHandler(Command.CONNECT,
                new IMockMessageHandler() {
                    @Override
                    public Frame respond(Frame request) {
                        Frame response = new Frame(Command.CONNECTED);
                        response.session(UUID.randomUUID().toString());
                        return response;
                    }
                });

        client.getStub().getServer().registerHandler(Command.DISCONNECT, new IMockMessageHandler() {
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
        client = null;
    }


    @Test
    public void connect() {
        // initialize the client
        // client.init("localhost", 61613, "user", "pass", "UTF-8");
        client.connect();

        // test logic
        assertTrue(client.isConnected());
        client.close();
        assertFalse(client.isConnected());
    }

    @Test
    public void connectNoSessionIdInResponse() {
        client = MockConnectionBuilder.mockClient().build();

        // register handlers
        client.getStub().getServer().registerHandler(Command.CONNECT,
                new IMockMessageHandler() {
                    @Override
                    public Frame respond(Frame request) {
                        Frame response = new Frame(Command.CONNECTED);
                        return response;
                    }
                });
        // initialize the client
        // client.init("localhost", 61613, "user", "pass", "UTF-8");
        client.connect();
    }

    @Test(expected = StompProtocolException.class)
    public void connectHandshakeError() {
        // create client object
        MockConnectionImpl conn = MockConnectionBuilder.mockClient().build();
        // register handlers
        conn.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.MESSAGE.getName(), false);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });
        // initialize the client
        conn.connect();
    }

    @Test(expected = StompConnectionException.class)
    public void notInitializedError() {
        MockConnectionImpl connection = MockConnectionBuilder.mockClient().build();
        connection.send(new Frame(Command.MESSAGE.getName()));
    }

    @Test(expected = StompConnectionException.class)
    public void doubleInitError() {
        // initialize the client
        client.connect();
        client.connect();
        // client.init("localhost", 61613, "user", "pass", "UTF-8");
        // client.init("localhost", 61613, "user", "pass", "UTF-8");
    }

    @Test
    public void errorStateCheck() {
        // initialize the client
        Exception e = null;
        client.connect();
        // client.init("localhost", 61613, "user", "pass", null);
        try {
            client.connect();
            // client.init("localhost", 61613, "user", "pass", null);
        } catch (Exception ex) {
            e = ex;
        } finally {
            assertNotNull(e);
            assertEquals(e.getClass(), StompConnectionException.class);
        }
        client.send(new Frame(Command.ACK));
    }

    @Test(expected = NullPointerException.class)
    public void errorStateCheckSecondInit() {
//        // initialize the client
//        Exception e = null;
//        // client.init("localhost", 61613, "user", "pass", null);
//        client.init();
//        try {
//            // client.init("localhost", 61613, "user", "pass", null);
//            client.init();
//        } catch (Exception ex) {
//            e = ex;
//        } finally {
//            assertNotNull(e);
//        }
//        // client.init("localhost", 61613, "user", "pass", null);
//        client.hostname("localhost").port(61613).encoding(null).init();
        client = MockConnectionBuilder.mockClient()
                .hostname("localhost")
                .port(61613)
                .username("user")
                .password("pass")
                .timeout(100)
                .encoding(null)
                .build();
    }


    @Test
    public void checkAttributes() {
        // initialize the client
        // client.init("localhost", 61613, "user", "pass", "UTF-8", 100);
        client.connect();
        assertEquals(client.getHostname(), "localhost");
        assertEquals(client.getPort(), Integer.valueOf(61613));
        assertEquals(client.getUsername(), "user");
        assertEquals(client.getPassword(), "pass");
        assertEquals(client.getEncoding(), "UTF-8");
        assertEquals(client.getTimeout(), Integer.valueOf(100));
        assertNotNull(client.getSessionId());
    }

//    @Test(expected = StompConnectionException.class)
//    public void notMockedIOExceptionInSocketInit() {
//        Connection client = ClientImpl.create();
//        client.hostname("localhost").port(TestUtil.getFreePort());
//        client.init();
//    }

//    @Test(expected = StompConnectionException.class)
//    public void notMockedIOExceptionInStreamsInit() {
//        class MockConnectionImpl extends ClientImpl {
//            protected void initializeCommunication(int timeout) {
//                initializeSocket(timeout);
//                // initializeStreams(timeout);
//            }
//
//            public void initializeStreamsPublic() {
//                closeSocket();
//                initializeStreams(1000);
//            }
//
//            @Override
//            public synchronized void init() {
//                assertNew();
//                initializeCommunication(1000);
//                setState(StompResourceState.CONNECTING);
//                setState(StompResourceState.CONNECTED);
//            }
//        }
//
//        ClientImpl client = new MockConnectionImpl();
//        ServerSocket localmachine = null;
//        int port = 0;
//        try {
//            localmachine = new ServerSocket(0);
//            port = localmachine.getLocalPort();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        client.hostname("localhost").port(port);
//        MockConnectionImpl clientMock = ((MockConnectionImpl) client);
//        clientMock.init();
//        try {
//            localmachine.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        clientMock.initializeStreamsPublic();
//    }

//    @Test
//    public void testInheritanceHierarchyAndFactoryMethodsAccessibility() {
//        Connection client = ClientImpl.create().hostname("localhost");
//        client.port(123).password("123");
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void testParametersValidation() {
//        ClientImpl.create().password(null);
//    }

//    @Test(expected = RuntimeException.class)
//    public void testMarshallException() {
//        ClientStubImplMock conn = new ClientStubImplMock();
//        conn.init();
//        conn.marshallPublic(new Frame(Command.SEND));
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void testUnMarshallException() {
//        ClientStubImplMock conn = new ClientStubImplMock();
//        conn.init();
//        conn.unmarshallPublic();
//    }

}
