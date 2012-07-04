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
import org.reficio.cougar.StompIllegalTransactionStateException;
import org.reficio.cougar.StompInvalidHeaderException;
import org.reficio.cougar.core.FrameDecorator;
import org.reficio.cougar.domain.Ack;
import org.reficio.cougar.domain.Command;
import org.reficio.cougar.domain.Frame;
import org.reficio.cougar.impl.IMockMessageHandler;
import org.reficio.cougar.impl.MockConnectionBuilder;
import org.reficio.cougar.impl.MockTransactionalClientImpl;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-10
 * Time: 12:27 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class TransactionalConnectionTest {

    private MockTransactionalClientImpl txClient;
    private EmptyDecorator decorator;

    class EmptyDecorator implements FrameDecorator {
        @Override
        public void decorateFrame(Frame frame) {
        }
    }

    @Before
    public void initialize() {
        txClient = MockConnectionBuilder.mockTransactionalConnection().hostname("localhost")
                .port(61613).timeout(1000).build();
        decorator = new EmptyDecorator();
        // register handlers
        txClient.getStub().getServer().registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        txClient.getStub().getServer().registerHandler(Command.SUBSCRIBE, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.MESSAGE);
                response.messageId(UUID.randomUUID().toString());
                response.payload("Adelboden is cool :)");
                return response;
            }
        });

        txClient.getStub().getServer().registerHandler(Command.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
        // initialize the txClient
        txClient.connect();
    }

    @After
    public void cleanup() {
        txClient = null;
        decorator = null;
    }


    @Test
    public void ackInTransactiona() {
        txClient.begin();
        txClient.ack("msg1", decorator);
        txClient.commit();
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(5, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // ack
        Frame ack = frames.get(2);
        assertEquals(Command.ACK, ack.getCommand());
        assertEquals(transactionId, ack.transaction());
        // commit
        Frame commit = frames.get(3);
        assertEquals(Command.COMMIT, commit.getCommand());
        assertEquals(transactionId, commit.transaction());
        // disconnect
        Frame disconnect = frames.get(4);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }

    @Test
    public void send() {
        final String payload = "msg1";
        txClient.begin();
        txClient.send("queue1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.payload(payload);
            }
        });
        txClient.commit();
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(5, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // ack
        Frame message = frames.get(2);
        assertEquals(Command.SEND, message.getCommand());
        assertEquals(transactionId, message.transaction());
        assertEquals(payload, message.payload());
        // commit
        Frame commit = frames.get(3);
        assertEquals(Command.COMMIT, commit.getCommand());
        assertEquals(transactionId, commit.transaction());
        // disconnect
        Frame disconnect = frames.get(4);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }

    @Test(expected = StompInvalidHeaderException.class)
    public void transactionSetExplicitly() {
        final String payload = "msg1";
        txClient.send("queue1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.payload(payload);
                frame.transaction("tx1_will_cause_error");
            }
        });
    }

    @Test
    public void beginComit() {
        final String payload = "msg1";
        txClient.begin();
        txClient.commit();
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(4, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // commit
        Frame commit = frames.get(2);
        assertEquals(Command.COMMIT, commit.getCommand());
        assertEquals(transactionId, commit.transaction());
        // disconnect
        Frame disconnect = frames.get(3);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }

    @Test
    public void beginCommitDecorator() {
        final String payload = "msg1";
        txClient.begin();
        txClient.commit(decorator);
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(4, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // commit
        Frame commit = frames.get(2);
        assertEquals(Command.COMMIT, commit.getCommand());
        assertEquals(transactionId, commit.transaction());
        // disconnect
        Frame disconnect = frames.get(3);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }


    @Test
    public void beginAbort() {
        final String payload = "msg1";
        txClient.begin();
        txClient.rollback();
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(4, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // abort
        Frame abort = frames.get(2);
        assertEquals(Command.ABORT, abort.getCommand());
        assertEquals(transactionId, abort.transaction());
        // disconnect
        Frame disconnect = frames.get(3);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }

    @Test
    public void beginAbortDecorator() {
        final String payload = "msg1";
        txClient.begin();
        txClient.rollback(decorator);
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(4, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // begin
        Frame begin = frames.get(1);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // abort
        Frame abort = frames.get(2);
        assertEquals(Command.ABORT, abort.getCommand());
        assertEquals(transactionId, abort.transaction());
        // disconnect
        Frame disconnect = frames.get(3);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }


    @Test(expected = StompIllegalTransactionStateException.class)
    public void doubleBegin() {
        txClient.begin();
        txClient.begin();
    }

    @Test(expected = StompIllegalTransactionStateException.class)
    public void comitNoBegin() {
        txClient.commit();
    }

    @Test(expected = StompConnectionException.class)
    public void txUninitializedConnection() {
        MockTransactionalClientImpl conn = MockConnectionBuilder.mockTransactionalConnection().build();
        conn.begin();
    }

    @Test
    public void subscribeReceiveAckCheck() {
        txClient.subscribe("r/queue/1", new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.ack(Ack.CLIENT);
            }
        });
        txClient.begin();
        Frame frame = txClient.receive();
        txClient.ack(frame.messageId());
        txClient.close();
        List<Frame> frames = txClient.getServer().getFrames();
        assertEquals(5, frames.size());
        // connect
        Frame connect = frames.get(0);
        assertEquals(Command.CONNECT, connect.getCommand());
        // subscribe
        Frame subscribe = frames.get(1);
        assertEquals(Command.SUBSCRIBE, subscribe.getCommand());
        // begin
        Frame begin = frames.get(2);
        assertEquals(Command.BEGIN, begin.getCommand());
        assertNotNull(begin.transaction());
        String transactionId = begin.transaction();
        // abort
        Frame ack = frames.get(3);
        assertEquals(Command.ACK, ack.getCommand());
        assertEquals(transactionId, ack.transaction());
        // disconnect
        Frame disconnect = frames.get(4);
        assertEquals(Command.DISCONNECT, disconnect.getCommand());
    }

}
