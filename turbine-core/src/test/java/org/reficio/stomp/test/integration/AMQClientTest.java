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
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.Turbine;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-13
 * Time: 10:30 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQClientTest extends AbstractAMQIntegrationTest<Client> {

    @Override
    public Client createConnection() {
        return Turbine.client().hostname(HOSTNAME).port(PORT).buildAndInit();
    }

    @Test
    public void connect() {
        Client client = createConnection();
        assertTrue(client.isInitialized());
        client.close();
        assertFalse(client.isInitialized());
    }

    @Test
    public void connectNotUTF() {
        Client client = Turbine.client().hostname("localhost").port(61613).encoding("cp1252").build();
        client.init();
        assertTrue(client.isInitialized());
        client.close();
        assertFalse(client.isInitialized());
    }

    @Test
    public void send() throws Exception {
        Client client = createConnection();

        final String receiptSubscribe = UUID.randomUUID().toString();
        Frame frameSubscribe = new Frame(Command.SUBSCRIBE);
        frameSubscribe.destination(getQueueName());
        frameSubscribe.receipt(receiptSubscribe);
        client.send(frameSubscribe);
        Frame responseSubscribe = client.receive();
        assertNotNull(responseSubscribe);

        final String payload = "TEST MESSAGE";
        final String receiptId = UUID.randomUUID().toString();
        Frame frame = new Frame(Command.SEND);
        frame.destination(getQueueName());
        frame.payload(payload);
        frame.receipt(receiptId);
        client.send(frame);
        Frame receipt = client.receive();
        assertTrue(receipt.getCommand().equals(Command.RECEIPT));

        Frame receivedFrame = client.receive();
        assertNotNull(receivedFrame);
        assertEquals(payload, receivedFrame.payload());
        assertEquals(getQueueName(), receivedFrame.destination());

        client.close();
    }

}
