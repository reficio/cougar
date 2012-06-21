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

import org.junit.Test;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.TurbineConnectionBuilder;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-13
 * Time: 10:30 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQTxConnectionTest extends AbstractAMQIntegrationTest<TransactionalConnection> {

    public TransactionalConnection createConnection() {
        return TurbineConnectionBuilder.transactionalConnection().hostname(HOSTNAME).port(PORT).buildAndConnect();
    }

    @Test
    public void connect() {
        TransactionalConnection conn = createConnection();
        assertTrue(conn.isInitialized());
        conn.close();
        assertFalse(conn.isInitialized());
    }

    @Test
    public void sendRollback() throws Exception {
        assertEquals(0, getQueueLength());

        final int NUMBER_OF_MSGS = 100;
        TransactionalConnection connSender = createConnection();
        connSender.begin();
        for (int i = 0; i < NUMBER_OF_MSGS; i++)
            connSender.send(getQueueName(), new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(System.currentTimeMillis() + "");
                }
            });
        assertEquals(0, getQueueLength());

        connSender.rollback();
        connSender.close();

        assertEquals(0, getQueueLength());
    }

}