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
import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.MockFactoryConnectionImpl;
import org.reficio.stomp.impl.TurbineConnectionFactory;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-11
 * Time: 09:19 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ConnectionFactoryTest {

    private Runnable startMockServer() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    ServerSocket srv = new ServerSocket(61613);
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
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return runnable;
    }

    @Test(timeout = 4000)
    public void createConnection() {
        startMockServer();
        TurbineConnectionFactory<Connection> factory = new TurbineConnectionFactory<Connection>(Connection.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");
        factory.setTimeout(1000);
        Connection conn = factory.createConnection();

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
        TurbineConnectionFactory<Connection> factory = new TurbineConnectionFactory<Connection>(Connection.class);
        Connection conn = factory.createConnection();
    }

    @Test(expected = StompException.class)
    public void createConnectionEx() {
        TurbineConnectionFactory<MockFactoryConnectionImpl> factory = new TurbineConnectionFactory<MockFactoryConnectionImpl>(MockFactoryConnectionImpl.class);
        factory.createConnection();
    }


}


