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
import org.reficio.stomp.StompInvalidHeaderException;
import org.reficio.stomp.core.FrameBuilder;
import org.reficio.stomp.domain.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-10
 * Time: 10:04 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class FrameBuilderTest {

    @Test
    public void connect() {
        Frame frame = new Frame(CommandType.CONNECT);
        frame.disableValidation();
        // frame.contentLength("123");

        Header header1 = frame.getHeader(HeaderType.CONTENT_LENGTH);
        Header header2 = frame.getHeader(HeaderType.CONTENT_LENGTH.getName());
        assertEquals(header1, header2);

        String val1 = frame.getHeaderValue(HeaderType.CONTENT_LENGTH);
        String val2 = frame.getHeaderValue(HeaderType.CONTENT_LENGTH.getName());
        assertEquals(val1, val2);
        assertNull(frame.getHeaderValue("adelboden_daenk"));
    }

    @Test
    public void custom() {
        Frame frame = new Frame(CommandType.SEND);
        frame.custom(HeaderType.CONTENT_LENGTH.getName(), "123");
        assertEquals("123", frame.contentLength());
        assertEquals("123", frame.custom(HeaderType.CONTENT_LENGTH.getName()));
    }

    @Test
    public void customNotDefined() {
        Frame frame = new Frame(CommandType.CONNECT);
        frame.custom("bam_correlation_id", "bus_proc_123");
        assertEquals("bus_proc_123", frame.custom("bam_correlation_id"));
    }

    @Test
    public void contentLength() {
        Frame frame = new Frame(CommandType.SEND);
        frame.payload("payload");
        assertEquals("payload", frame.payload());
        assertEquals("7", frame.contentLength());

        Frame frame2 = new Frame(CommandType.MESSAGE);
        frame2.payload("payload");
        assertEquals("payload", frame2.payload());
        assertEquals("7", frame2.contentLength());

        Frame frame3 = new Frame(CommandType.ERROR);
        frame3.payload("payload");
        assertEquals("payload", frame3.payload());
        assertEquals("7", frame3.contentLength());

        frame3.payload(null);
        assertEquals(null, frame3.payload());
        assertEquals(null, frame3.contentLength());

    }

    @Test
    public void headers() {
        Frame frame = new Frame(CommandType.BEGIN);
        frame.disableValidation();
        frame.payload("payload");
        assertEquals("payload", frame.payload());
        // begin can not have contentLength
        assertEquals(frame.contentLength(), null);

        frame.payload(null);
        assertEquals(null, frame.payload());

        frame.payload("payload", true);
        assertEquals("payload", frame.payload());
        assertEquals(frame.contentLength(), null);

        assertNull(frame.ack());
        frame.login("login");
        assertEquals("login", frame.login());
        frame.encoding("encoding");
        assertEquals("encoding", frame.encoding());
        frame.subscription("subscription");
        assertEquals("subscription", frame.subscription());
        frame.session("session");
        assertEquals("session", frame.session());
        frame.passcode("passcode");
        assertEquals("passcode", frame.passcode());
        frame.destination("destination");
        assertEquals("destination", frame.destination());
        frame.ack(AckType.AUTO);
        assertEquals(AckType.AUTO, frame.ack());
        frame.transaction("transaction");
        assertEquals("transaction", frame.transaction());
        frame.receipt("receipt");
        assertEquals("receipt", frame.receipt());
        frame.errorMessageContent("errorMessageContent");
        assertEquals("errorMessageContent", frame.errorMessageContent());
        // frame.contentLength("contentLength");
        // assertEquals("contentLength", frame.contentLength());
        frame.receiptId("receiptId");
        assertEquals("receiptId", frame.receiptId());
        frame.messageId("messageId");
        assertEquals("messageId", frame.messageId());
        frame.subscriptionId("subscriptionId");
        assertEquals("subscriptionId", frame.subscriptionId());
        frame.selector("selector");
        assertEquals("selector", frame.selector());
        frame.custom("custom_header", "custom_value");
        assertEquals("custom_value", frame.custom("custom_header"));
    }

    @Test(expected = StompInvalidHeaderException.class)
    public void ackCustomValidation() {
        Frame frame = new Frame(CommandType.ACK);
        frame.custom("ack", "exception");
    }

    @Test(expected = StompInvalidHeaderException.class)
    public void frozenFreeze() {
        class FrameFreeze extends Frame {
            public FrameFreeze(CommandType command) {
                super(command);
            }
            public void freezePublic() {
                freeze();
            }

        };
        FrameFreeze frame = new FrameFreeze(CommandType.COMMIT);
        frame.custom("test", "ok");
        frame.freezePublic();
        // double freeze for testing purposes
        frame.freezePublic();
        frame.custom("test", "exception");

    }


}
