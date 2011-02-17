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

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.reficio.stomp.StompIOException;
import org.reficio.stomp.StompWireFormatException;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.domain.HeaderType;
import org.reficio.stomp.impl.WireFormatImpl;
import org.reficio.stomp.util.SubscriptionRegister;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-29
 * Time: 12:58 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class WireFormatTest {

    @Test
    public void marshall() {
        String hLogin = "test_login";
        String hPassCode = "test_passcode";
        String hEncoding = "UTF-8";
        String payload = "test_payload";

        Frame frame = new Frame(CommandType.CONNECT);
        frame.login(hLogin);
        frame.passcode(hPassCode);
        frame.encoding(hEncoding);
        frame.payload(payload);

        StringWriter writer = new StringWriter();
        StompWireFormat wireFormat = new WireFormatImpl();
        wireFormat.marshal(frame, writer);

        String expectedResult =
                CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE +
                HeaderType.ENCODING.getName() + WireFormatImpl.HEADER_DELIMITER + hEncoding + WireFormatImpl.END_OF_LINE +
                HeaderType.LOGIN.getName() + WireFormatImpl.HEADER_DELIMITER + hLogin + WireFormatImpl.END_OF_LINE +
                HeaderType.PASS_CODE.getName() + WireFormatImpl.HEADER_DELIMITER + hPassCode + WireFormatImpl.END_OF_LINE +
                WireFormatImpl.END_OF_LINE+
                payload+WireFormatImpl.END_OF_FRAME;

        assertEquals(expectedResult, writer.toString());
    }

    @Test(expected = StompIOException.class)
    public void marshallIOException() throws IOException {
        String hLogin = "test_login";
        String hPassCode = "test_passcode";
        String hEncoding = "UTF-8";
        String payload = "test_payload";

        Frame frame = new Frame(CommandType.CONNECT);
        frame.login(hLogin);
        frame.passcode(hPassCode);
        frame.encoding(hEncoding);
        frame.payload(payload);

        Writer writer = mock(Writer.class);
        doThrow(new IOException()).when(writer).flush();

        StompWireFormat wireFormat = new WireFormatImpl();
        wireFormat.marshal(frame, writer);

        String expectedResult =
                CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE +
                HeaderType.ENCODING.getName() + WireFormatImpl.HEADER_DELIMITER + hEncoding + WireFormatImpl.END_OF_LINE +
                HeaderType.LOGIN.getName() + WireFormatImpl.HEADER_DELIMITER + hLogin + WireFormatImpl.END_OF_LINE +
                HeaderType.PASS_CODE.getName() + WireFormatImpl.HEADER_DELIMITER + hPassCode + WireFormatImpl.END_OF_LINE +
                WireFormatImpl.END_OF_LINE+
                payload+WireFormatImpl.END_OF_FRAME;

        assertEquals(expectedResult, writer.toString());
    }

    @Test
    public void unmarshall() {
        String hLogin = "test_login";
        String hPassCode = "test_passcode";
        String hEncoding = "UTF-8";
        String payload = "test_payload";

        String marshalledFrame =
                CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE +
                HeaderType.ENCODING.getName() + WireFormatImpl.HEADER_DELIMITER + hEncoding + WireFormatImpl.END_OF_LINE +
                HeaderType.LOGIN.getName() + WireFormatImpl.HEADER_DELIMITER + hLogin + WireFormatImpl.END_OF_LINE +
                HeaderType.PASS_CODE.getName() + WireFormatImpl.HEADER_DELIMITER + hPassCode + WireFormatImpl.END_OF_LINE +
                WireFormatImpl.END_OF_LINE+
                payload+WireFormatImpl.END_OF_FRAME;

        StringReader reader = new StringReader(marshalledFrame);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(reader);

        assertEquals(frame.login(), hLogin);
        assertEquals(frame.passcode(), hPassCode);
        assertEquals(frame.encoding(), hEncoding);
        assertEquals(frame.payload(), payload);
    }

    @Test(expected = StompWireFormatException.class)
    public void parseCommandException() {
        String hLogin = "test_login";
        String hPassCode = "test_passcode";
        String hEncoding = "UTF-8";
        String payload = "test_payload";

        String marshalledFrame =
                "YO YO FEEL THE FLOW COMMAND" + WireFormatImpl.END_OF_LINE +
                HeaderType.ENCODING.getName() + WireFormatImpl.HEADER_DELIMITER + hEncoding + WireFormatImpl.END_OF_LINE +
                HeaderType.LOGIN.getName() + WireFormatImpl.HEADER_DELIMITER + hLogin + WireFormatImpl.END_OF_LINE +
                HeaderType.PASS_CODE.getName() + WireFormatImpl.HEADER_DELIMITER + hPassCode + WireFormatImpl.END_OF_LINE +
                WireFormatImpl.END_OF_LINE+
                payload+WireFormatImpl.END_OF_FRAME;

        StringReader reader = new StringReader(marshalledFrame);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(reader);
    }

    @Test
    public void bufferedReaderTest() {
        String hLogin = "test_login";
        String hPassCode = "test_passcode";
        String hEncoding = "UTF-8";
        String payload = "test_payload";

        String marshalledFrame =
                CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE +
                HeaderType.ENCODING.getName() + WireFormatImpl.HEADER_DELIMITER + hEncoding + WireFormatImpl.END_OF_LINE +
                HeaderType.LOGIN.getName() + WireFormatImpl.HEADER_DELIMITER + hLogin + WireFormatImpl.END_OF_LINE +
                HeaderType.PASS_CODE.getName() + WireFormatImpl.HEADER_DELIMITER + hPassCode + WireFormatImpl.END_OF_LINE +
                WireFormatImpl.END_OF_LINE+
                payload+WireFormatImpl.END_OF_FRAME;

        StringReader reader = new StringReader(marshalledFrame);
        BufferedReader buffReader = new BufferedReader(reader);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(buffReader);
    }

    @Test(expected = StompWireFormatException.class)
    public void headersCountExceeded() {
        String payload = "test_payload";
        String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
        for(int i = 0 ; i < WireFormatImpl.MAX_HEADERS + 1 ; i++) {
            marshalledFrame += "header"+i + WireFormatImpl.HEADER_DELIMITER + "header_value" + WireFormatImpl.END_OF_LINE;
        }
        marshalledFrame += WireFormatImpl.END_OF_LINE+payload+WireFormatImpl.END_OF_FRAME;
        StringReader reader = new StringReader(marshalledFrame);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void headerSplitCondition1() {
        String payload = "test_payload";
        String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
        marshalledFrame += "header" + "header_value" + WireFormatImpl.END_OF_LINE;
        marshalledFrame += WireFormatImpl.END_OF_LINE+payload+WireFormatImpl.END_OF_FRAME;
        StringReader reader = new StringReader(marshalledFrame);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void headerSplitCondition2() {
        String payload = "test_payload";
        String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
        marshalledFrame += WireFormatImpl.HEADER_DELIMITER+WireFormatImpl.END_OF_LINE;
        marshalledFrame += WireFormatImpl.END_OF_LINE+payload+WireFormatImpl.END_OF_FRAME;
        StringReader reader = new StringReader(marshalledFrame);
        StompWireFormat wireFormat = new WireFormatImpl();
        Frame frame = wireFormat.unmarshal(reader);
    }

    @Test
    public void headerContentLengthHeaderNumericException() {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + "ThisHeaderWillBeIgnoredDueToNumericException:)" +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
       assertNotNull(frame);
    }

    @Test(expected = StompWireFormatException.class)
    public void maxCommandLengthExceeded() {
       String payload = "test_payload";
       String commandString = RandomStringUtils.random(WireFormatImpl.MAX_COMMAND_LENGTH + 1);
       String marshalledFrame = commandString + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + "1000" +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void maxHeaderLengthExceeded() {
       String payload = "test_payload";
       String headerPrefix = "header" + WireFormatImpl.HEADER_DELIMITER;
       String headerValue = RandomStringUtils.random(WireFormatImpl.MAX_HEADER_LENGTH - headerPrefix.length() + 1);
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += headerPrefix + headerValue + WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void endOfStreamInReadUntilEndMarker() {
       String payload = "test_payload";
       String headerPrefix = "header" + WireFormatImpl.HEADER_DELIMITER;
       String headerValue = "value";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += headerPrefix + headerValue;
       // marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompIOException.class)
    public void ioExceptionInReadUntilEndMarker() throws IOException {
       Reader reader = mock(BufferedReader.class);
       doThrow(new IOException()).when(reader).read();
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test
    public void parsePayloadWithContentLength() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()) +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void parsePayloadWithContentLengthTooSmall() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()-1) +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void parsePayloadWithContentLengthTooBigSpecialCase() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()+1) +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompWireFormatException.class)
    public void parsePayloadWithContentLengthTooBig() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()+2) +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(reader);
    }

    @Test(expected = StompIOException.class)
    public void ioExceptionInparsePayload() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()) +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       Reader reader = new BufferedReader(new StringReader(marshalledFrame));

       char c[]=new char[payload.length()];
       Reader spy = spy(reader);
       doThrow(new IOException()).when(spy).read(c,0,payload.length());
       StompWireFormat wireFormat = new WireFormatImpl();
       Frame frame = wireFormat.unmarshal(spy);
    }

//    @Test(expected = StompWireFormatException.class)
//    public void ioExceptionInparsePayload() throws IOException {
//       String payload = "test_payload";
//       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
//       marshalledFrame += HeaderType.CONTENT_LENGTH.getName() + WireFormatImpl.HEADER_DELIMITER + (payload.length()+2) +WireFormatImpl.END_OF_LINE;
//       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
//       StringReader reader = new StringReader(marshalledFrame);
//       StompWireFormat wireFormat = new WireFormatImpl();
//
//       Reader reader = mock(BufferedReader.class);
//       doThrow(new IOException()).when(reader).read();
//       StompWireFormat wireFormat = new WireFormatImpl();
//       Frame frame = wireFormat.unmarshal(reader);
//
//       Frame frame = wireFormat.unmarshal(reader);
//    }

    @Test
    public void registerUsage() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.SUBSCRIPTION_ID.getName() + WireFormatImpl.HEADER_DELIMITER + "123" +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl(new SubscriptionRegister());
       Frame frame = wireFormat.unmarshal(reader);
       assertFalse(frame.isSubscribionValid());
    }

    @Test
    public void registerUsageWithoutId() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       // marshalledFrame += HeaderType.SUBSCRIPTION_ID.getName() + WireFormatImpl.HEADER_DELIMITER + "123" +WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl(new SubscriptionRegister());
       Frame frame = wireFormat.unmarshal(reader);
       assertNull(frame.isSubscribionValid());
    }

    @Test
    public void registerUsageWithNullId() throws IOException {
       String payload = "test_payload";
       String marshalledFrame = CommandType.CONNECT.getName() + WireFormatImpl.END_OF_LINE;
       marshalledFrame += HeaderType.SUBSCRIPTION_ID.getName() + WireFormatImpl.HEADER_DELIMITER + WireFormatImpl.END_OF_LINE;
       marshalledFrame += WireFormatImpl.END_OF_LINE + payload + WireFormatImpl.END_OF_FRAME;
       StringReader reader = new StringReader(marshalledFrame);
       StompWireFormat wireFormat = new WireFormatImpl(new SubscriptionRegister());
       Frame frame = wireFormat.unmarshal(reader);
       assertNull(frame.isSubscribionValid());
    }

    @Test
    public void getVersion() {
        // cool to have 100% coverage in such a vital test:)
       StompWireFormat wireFormat = new WireFormatImpl(new SubscriptionRegister());
       wireFormat.getVersion();
    }

}
