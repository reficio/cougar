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
package org.reficio.cougar.impl;

import org.junit.Test;
import org.reficio.cougar.StompConnectionException;
import org.reficio.cougar.StompEncodingException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertNotNull;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class ConnectionSocketTest {

    public class TestConnectionImpl extends ConnectionImpl {
        protected TestConnectionImpl(String encoding) {
            super(new WireFormatImpl());
            hostname("localhost");
            port(32611);
            encoding(encoding);
            timeout(1000);
            postConstruct();
        }

        protected void doConnect() {
            // skip the handshake as it's only a socket connection test
        }
    }

    @Test
    public void connectAndClose() throws IOException {
        ServerSocket srv = new ServerSocket(32611);
        try {
            TestConnectionImpl client = new TestConnectionImpl("UTF-8");
            client.connect();
            Socket comm = srv.accept();
            assertNotNull(comm);
            client.close();
        } finally {
            srv.close();
        }
    }

    @Test(expected = StompEncodingException.class)
    public void unsupportedEncoding() throws IOException {
        ServerSocket srv = new ServerSocket(32611);
        try {
            TestConnectionImpl client = new TestConnectionImpl("NO_SUCH_ENCODING");
            client.connect();
        } finally {
            srv.close();
        }
    }

    @Test(expected = StompConnectionException.class)
    public void connectionError() throws IOException {
        TestConnectionImpl client = new TestConnectionImpl("UTF-8");
        client.connect();
    }

}
