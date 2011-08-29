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

import org.junit.Test;
import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.impl.ConnectionImpl;
import org.reficio.stomp.impl.StompConnectionFactory;
import org.reficio.stomp.test.mock.MockFactoryConnectionImpl;
import org.reficio.stomp.test.mock.PrivateConstructorResource;

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

    abstract class CanInstantiateAbstract extends ConnectionImpl {
    }

    @Test
    public void createConnection() {
        StompConnectionFactory<Connection> factory = new StompConnectionFactory<Connection>(MockFactoryConnectionImpl.class);
        factory.setEncoding("UTF-8");
        factory.setHostname("localhost");
        factory.setPort(61613);
        factory.setUsername("system");
        factory.setPassword("manager");
        Connection conn = factory.createConnection();

        assertEquals(factory.getEncoding(), conn.getEncoding());
        assertEquals(factory.getHostname(), conn.getHostname());
        assertEquals(factory.getPort(), conn.getPort());
        assertEquals(factory.getUsername(), conn.getUsername());
        assertEquals(factory.getPassword(), conn.getPassword());
    }

    @Test(expected = StompException.class)
    public void createConnectionEx() {
        StompConnectionFactory<Connection> factory = new StompConnectionFactory<Connection>(CanInstantiateAbstract.class);
        factory.createConnection();
    }

    @Test(expected = StompException.class)
    public void createConnectionEx2() {
        StompConnectionFactory<PrivateConstructorResource> factory = new StompConnectionFactory<PrivateConstructorResource>(PrivateConstructorResource.class);
        factory.createConnection();
    }

}


