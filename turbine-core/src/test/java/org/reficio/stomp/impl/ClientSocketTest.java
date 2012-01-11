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
package org.reficio.stomp.impl;

import org.junit.Test;
import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.impl.ClientImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class ClientSocketTest {

    public class TestClientImpl extends ClientImpl {
        protected TestClientImpl() {
            super(null);
        }
        protected void connect() {
        }
    }

    @Test
    public void connectAndClose() throws IOException {
        ServerSocket srv = null;
        srv = new ServerSocket(32611);
        Client client = new TestClientImpl();
        client.hostname("localhost").port(32611).init();
        Socket comm = srv.accept();
        assertNotNull(comm);
        client.close();
        srv.close();
    }

    @Test(expected = StompEncodingException.class)
    public void unsupportedEncoding() throws IOException {
        ServerSocket srv = null;
        srv = new ServerSocket(32611);
//        Client client = new TestClientImpl();
//        client.init("localhost", 32611, "user", "pass", "NO_SUCH_ENCODING");
        Client client = new TestClientImpl();
        client.hostname("localhost").port(32611).encoding("NO_SUCH_ENCODING").init();
    }

    @Test(expected = StompConnectionException.class)
    public void connectionError() throws IOException {
//        Client client = new TestClientImpl();
//        client.init("localhost", 32612, "user", "pass", "UTF-8");
        Client client = new TestClientImpl();
        client.hostname("localhost").port(32612).encoding("UTF-8").init();
    }

    @Test
    public void receive() throws IOException {
//        ServerSocket srv = null;
//        srv = new ServerSocket(32611);
//        Client client = new TestClientImpl();
//        client.init("localhost", 32611, "user", "pass", "UTF-8");
//        srv.close();
//        client.close();
    }

}
