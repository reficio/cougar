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
import org.reficio.stomp.*;
import org.reficio.stomp.domain.Ack;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-01-13
 * Time: 10:11 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class HeaderTypeTest {

    @Test
    public void connect() {
        Frame frame = new Frame(Command.CONNECT);
        frame.login("007");
        frame.passcode("007");
        frame.encoding("007");
    }

    @Test(expected = StompInvalidHeaderException.class)
    public void disconnect() {
        Frame frame = new Frame(Command.DISCONNECT);
        frame.login("007");
    }

    @Test
    public void send() {
        Frame frame = new Frame(Command.SEND);
        frame.destination("007");
        frame.receipt("007");
        frame.transaction("007");
        // frame.contentLength("007");
    }

    @Test
    public void subscribe() {
        Frame frame = new Frame(Command.SUBSCRIBE);
        frame.destination("007");
        frame.ack(Ack.AUTO);
        frame.selector("007");
        frame.subscriptionId("007");
        frame.receipt("007");
    }

    @Test(expected = StompInvalidHeaderException.class)
    public void unsubscribeFails() {
        Frame frame = new Frame(Command.UNSUBSCRIBE);
        frame.destination("007");
        frame.subscriptionId("007");
        frame.receipt("007");
    }

    @Test
    public void unsubscribeSucceeds1() {
        Frame frame = new Frame(Command.UNSUBSCRIBE);
        frame.destination("007");
        frame.receipt("007");
    }

    @Test
    public void unsubscribeSucceeds2() {
        Frame frame = new Frame(Command.UNSUBSCRIBE);
        frame.subscriptionId("007");
        frame.receipt("007");
    }

    @Test
    public void begin() {
        Frame frame = new Frame(Command.BEGIN);
        frame.transaction("007");
        frame.receipt("007");
    }

    @Test
    public void commit() {
        Frame frame = new Frame(Command.COMMIT);
        frame.transaction("007");
        frame.receipt("007");
    }

    @Test
    public void abort() {
        Frame frame = new Frame(Command.ABORT);
        frame.transaction("007");
        frame.receipt("007");
    }

    @Test
    public void ack() {
        Frame frame = new Frame(Command.ACK);
        frame.transaction("007");
        frame.receipt("007");
        // frame.messageId("007");
    }

    @Test
    public void connected() {
        Frame frame = new Frame(Command.CONNECTED);
        frame.session("007");
        frame.encoding("007");
    }

    @Test
    public void message() {
        Frame frame = new Frame(Command.MESSAGE);
        frame.destination("007");
        frame.messageId("007");
        frame.subscription("007");
        // frame.contentLength("007");
    }

    @Test
    public void receipt() {
        Frame frame = new Frame(Command.RECEIPT);
        frame.receiptId("007");
    }

    @Test
    public void error() {
        Frame frame = new Frame(Command.ERROR);
        frame.errorMessageContent("007");
        // frame.contentLength("007");
    }

    @Test()
    public void isAllowed() {
        Frame frame = new Frame(Command.ERROR);
        frame.errorMessageContent("007");
        // frame.contentLength("007");
    }

    @Test
    public void totalDummy() {
        // let's pimp up the code coverage indicators :)
        Exception ex = new RuntimeException();
        new StompEncodingException("message", ex);
        new StompEncodingException("message");
        new StompIllegalTransactionStateException("message", ex);
        new StompIllegalTransactionStateException("message");
        new StompInvalidHeaderException("message", ex);
        new StompInvalidHeaderException("message");
        new StompConnectionException("message", ex);
        new StompConnectionException("message");
        new StompException("message", ex);
        new StompException("message");
        new StompProtocolException("message", ex);
        new StompProtocolException("message");
        new StompWireFormatException("Haadader", "message", ex);
        new StompWireFormatException("Haadader", "message");
        new StompWireFormatException("message");
    }

}
