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

import org.junit.Ignore;
import org.junit.Test;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.impl.ConnectionBuilder;
import org.reficio.cougar.util.DisconnectingReceiver;
import org.reficio.cougar.util.Receiver;
import org.reficio.cougar.util.Sender;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-12
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class AMQConnectionIntegrationTest extends AbstractAMQIntegrationTest<Client> {

    @Test
    public void connect() {
        Client conn = createConnection();
        assertTrue(conn.isConnected());
        conn.close();
        assertFalse(conn.isConnected());
    }

    @Test
    public void singleSendReceive() throws Exception {
        final String receiptId = UUID.randomUUID().toString();
        final String payload = "James Bond 007!";
        Client connSender = createConnection();
        connSender.send(getQueueName(), new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.payload(payload);
                frame.receipt(receiptId);
            }
        });
        Frame receipt = connSender.receive();
        assertEquals(receiptId, receipt.receiptId());

        assertEquals(1, getQueueLength());

        Client connReceiver = createConnection();
        connReceiver.subscribe(getQueueName());
        Frame frame = connReceiver.receive();
        assertNotNull(frame);
        assertEquals(payload, frame.payload());
        assertEquals(getQueueName(), frame.destination());
        connReceiver.close();
        connSender.close();
    }

    @Test
    public void consecutiveSendReceive() throws Exception {
        final int NUMBER_OF_MSGS = 100;
        Client connSender = createConnection();
        for (int i = 0; i < NUMBER_OF_MSGS; i++) {
            connSender.send(getQueueName(), new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(System.currentTimeMillis() + "");
                }
            });
        }
        connSender.close();

        Client connReceiver = createConnection();
        String subId = connReceiver.subscribe(getQueueName());
        for (int i = 0; i < NUMBER_OF_MSGS; i++) {
            assertNotNull(connReceiver.receive());
        }
        connReceiver.unsubscribe(subId);
        connReceiver.close();
        connReceiver = null;
    }


    @Test
    public void parallelSendReceive() throws Exception {
        String queue = getQueueName();
        AtomicInteger counter = new AtomicInteger(0);

        Sender sender1 = new Sender(HOSTNAME, PORT);
        Sender sender2 = new Sender(HOSTNAME, PORT);
        Receiver receiver1 = new Receiver(HOSTNAME, PORT);
        Receiver receiver2 = new Receiver(HOSTNAME, PORT);

        receiver1.execute(counter, queue, 200);
        receiver2.execute(counter, queue, 200);
        sender1.execute(queue, 100);
        sender2.execute(queue, 100);

        receiver1.join();
        receiver2.join();
        sender1.join();
        sender2.join();

        assertEquals(sender1.getSent() + sender2.getSent(), receiver1.getReceived() + receiver2.getReceived());
    }

    @Test
    public void parallelSendReceiveDisconnectAfterReception() throws Exception {
        String queue = getQueueName();
        AtomicInteger counter = new AtomicInteger(0);
        boolean autoAck = false;

        Sender sender1 = new Sender(HOSTNAME, PORT);
        Sender sender2 = new Sender(HOSTNAME, PORT);
        DisconnectingReceiver receiver1 = new DisconnectingReceiver(HOSTNAME, PORT);
        DisconnectingReceiver receiver2 = new DisconnectingReceiver(HOSTNAME, PORT);

        receiver1.execute(counter, queue, 200, autoAck);
        receiver2.execute(counter, queue, 200, autoAck);
        sender1.execute(queue, 100);
        sender2.execute(queue, 100);

        receiver1.join();
        receiver2.join();
        sender1.join();
        sender2.join();

        assertEquals(sender1.getSent() + sender2.getSent(), receiver1.getReceived() + receiver2.getReceived());
    }

    @Override
    public Client createConnection() {
        return ConnectionBuilder.connection().hostname(HOSTNAME).port(PORT).buildAndConnect();
    }

}
