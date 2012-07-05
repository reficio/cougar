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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.reficio.cougar.StompException;
import org.reficio.cougar.connection.Client;
import org.reficio.cougar.core.StompWireFormat;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.factory.SimpleConnectionFactory;
import org.reficio.cougar.util.TestUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-11
 * Time: 09:19 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ConnectionFactoryTest {

    private final Log log = LogFactory.getLog(ConnectionFactoryTest.class);

    private int startMockServer() {
        final int port = TestUtil.getFreePort();
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    ServerSocket srv = new ServerSocket(port);
                    try {
                        srv.setSoTimeout(2000);
                        Socket comm = srv.accept();
                        Frame response = new Frame(Command.CONNECTED);
                        response.session(UUID.randomUUID().toString());
                        StompWireFormat wireFormat = new WireFormatImpl();
                        OutputStream out = comm.getOutputStream();
                        Writer writer = new OutputStreamWriter(out);
                        wireFormat.marshal(response, writer);
                        writer.close();
                    } finally {
                        srv.close();
                    }
                } catch (IOException e) {
                    log.error("IO exception", e);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return port;
    }

    @Test(timeout = 4000)
    public void createConnection() {
        int port = startMockServer();
        SimpleConnectionFactory<Client> factory = new SimpleConnectionFactory<Client>(Client.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(port);
        factory.setUsername("system");
        factory.setPassword("manager");
        factory.setTimeout(1000);
        Client conn = factory.createConnection();

        assertEquals(factory.getEncoding(), conn.getEncoding());
        assertEquals(factory.getHostname(), conn.getHostname());
        assertEquals(factory.getPort(), conn.getPort());
        assertEquals(factory.getUsername(), conn.getUsername());
        assertEquals(factory.getPassword(), conn.getPassword());
        assertEquals(factory.getTimeout(), conn.getTimeout());
    }

    @Test(timeout = 4000)
    public void createConnectionDefault() {
        startMockServer();
        SimpleConnectionFactory<Client> factory = new SimpleConnectionFactory<Client>(Client.class);
        Client conn = factory.createConnection();
    }

    @Test(expected = StompException.class)
    public void createConnectionEx() {
        class NotSupportedConnectionImpl extends MockConnectionImpl {
        }
        SimpleConnectionFactory<NotSupportedConnectionImpl> factory = new SimpleConnectionFactory<NotSupportedConnectionImpl>(NotSupportedConnectionImpl.class);
        factory.createConnection();
    }


}


