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

import org.apache.commons.lang.StringUtils;
import org.reficio.stomp.StompIOException;
import org.reficio.stomp.StompSocketTimeoutException;
import org.reficio.stomp.StompWireFormatException;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.domain.Header;
import org.reficio.stomp.domain.HeaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
class WireFormatImpl implements StompWireFormat {

    private static final transient Logger log = LoggerFactory.getLogger(WireFormatImpl.class);

    public static final String VERSION = "1";

    public static final char END_OF_FRAME = '\u0000';
    public static final char END_OF_LINE = '\n';
    public static final char HEADER_DELIMITER = ':';

    public static final int MAX_COMMAND_LENGTH = 1024;
    public static final int MAX_HEADER_LENGTH = 1024 * 10;
    public static final int MAX_HEADERS = 1000;
    public static final int MAX_PAYLOAD_LENGTH = 1024 * 1024 * 512; // 512MB

    public static final int AVG_PAYLOAD_SIZE = 1024 * 16; // 16KB

    public WireFormatImpl() {
		super();
	}

    private String toWireFormat(Header header) {
		return String.format("%s:%s", header.getName(), header.getValue());
	}

    @Override
    public void marshal(Frame frame, Writer output) {
        StringBuilder builder = new StringBuilder();
        // command name followed by new line
		builder.append(frame.getCommandName());
        builder.append(END_OF_LINE);
		// headers - each followed by new line
        for(Header header : frame.getHeaders()) {
			builder.append(toWireFormat(header));
            builder.append(END_OF_LINE);
		}
        // end of headers marker
		builder.append(END_OF_LINE);
        // payload string
        if(StringUtils.isNotBlank(frame.payload())) {
            builder.append(frame.payload());
        }
        builder.append(END_OF_FRAME);
        try {
            output.write(builder.toString());
            output.flush();
        } catch (IOException e) {
            throw new StompIOException("Error during data send", e);
        }
        frame.freeze();
    }

    @Override
    public Frame unmarshal(Reader reader) {
        try {
            Command command = parseCommand(reader);
            Map<String, Header> headers = parseHeaders(reader);
            String payload = parsePayload(reader, parseContentLength(headers));
            Frame result = new Frame(command, headers, payload);
            result.freeze();
            return result;
        } catch(StompWireFormatException ex) {
            throw ex;
        }
    }

    private String readLine(Reader input, int maxLength, String errorMessage, boolean skipLeadingEndMarkers){
        return readUntilEndMarker(input, maxLength, END_OF_LINE, errorMessage, skipLeadingEndMarkers);
    }

    private Command parseCommand(Reader input) {
        String commandString = readLine(input, MAX_COMMAND_LENGTH, "Error during command parsing", true);
        Command command = Command.getCommand(commandString.trim());
        if(command == null) {
            throw new StompWireFormatException(commandString, String.format("Command [%s] not recognized", commandString));
        }
        return command;
    }

    private Map<String, Header> parseHeaders(Reader input) {
        Map<String, Header> headers = new HashMap<String, Header>();
        int headersCount = 0;
        while(true) {
            String headerString = readLine(input, MAX_HEADER_LENGTH, "Error during header parsing", false);
            headerString = headerString.trim();
            if(headerString.length() == 0) {
                break;
            }
            if(headersCount >= MAX_HEADERS) {
                throw new StompWireFormatException("Number of headers exceeded");
            }
            int offset = headerString.indexOf(HEADER_DELIMITER);
            if(offset <= 0) {
                throw new StompWireFormatException(headerString, "Error during header split");
            }
            Header header = Header.createHeader(headerString.substring(0, offset), headerString.substring(offset+1));
            headers.put(header.getName(), header);
            headersCount++;
        }
        return headers;
    }

    private Integer parseContentLength(Map<String, Header> headers) {
        Header contentLengthHeader = headers.get(HeaderType.CONTENT_LENGTH.getName());
        try {
            return contentLengthHeader != null ? Integer.parseInt(contentLengthHeader.getValue()) : null;
        } catch(NumberFormatException ex) {
            log.warn("Error during content length parsing - header is ignored.");
            return null;
        }
    }

    private String parsePayload(Reader input, Integer contentLength) {
        try {
            if (contentLength != null) {
                char[] payloadChars = new char[contentLength];
                int charsRead = input.read(payloadChars, 0, contentLength);
                if(charsRead != contentLength) {
                    throw new StompWireFormatException("Mismatch during content read. Wrong content-length header! Content-length header value TOO BIG.");
                }
                int nextByte = input.read();
                if(nextByte < 0) {
                    throw new StompIOException("End of stream has been reached");
                }
                if(nextByte!=END_OF_FRAME) {
                    throw new StompWireFormatException("Mismatch during content read. Wrong content-length header! Content-length header value TOO SMALL.");
                }
                return new String(payloadChars);
            } else {
                return readUntilEndMarker(input, MAX_PAYLOAD_LENGTH, END_OF_FRAME, "Error during payload parsing", false);
            }
        } catch (IOException e) {
            throw new StompIOException("Error during payload data receipt", e);
        }
    }

    private String readUntilEndMarker(Reader input, int maxLength, char endMarker, String errorMessage, boolean skipLeadingEndMarkers) {
       int currentByte;
       try {
           StringBuilder output = new StringBuilder(AVG_PAYLOAD_SIZE);
           boolean receivedContent = false;
           while (true) {
               if (output.length() > maxLength) {
                   throw new StompWireFormatException("Max length exceeded");
               }
               currentByte = input.read();
               if(currentByte < 0) {
                   throw new StompIOException("End of stream has been reached");
               } else if((char)currentByte == endMarker) {
                   if(receivedContent || !skipLeadingEndMarkers) {
                       break;
                   } else {
                       continue;
                   }
               } else {
                   output.append((char)currentByte);
                   receivedContent = true;
               }
           }
           return output.toString();
       } catch(SocketTimeoutException ex) {
           throw new StompSocketTimeoutException(ex);
       } catch(IOException ex) {
           throw new StompIOException(errorMessage, ex);
       }
    }

    public String getVersion() {
        return VERSION;
    }

}