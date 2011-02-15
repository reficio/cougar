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

package org.reficio.stomp.test.mock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.WireFormatImpl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-27
 * Time: 03:41 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class MockServer implements Runnable {

    private final Log logger = LogFactory.getLog(MockServer.class);

    public Writer writer;
    public Reader reader;
    private StompWireFormat wireFormat;
    private List<Frame> receivedFrames;

    private Map<CommandType, IMockMessageHandler> typeHandlers;
    private Map<String, IMockMessageHandler> idHandlers;

    public MockServer() {
        this.wireFormat = new WireFormatImpl();
        this.receivedFrames = new CopyOnWriteArrayList<Frame>();

        this.typeHandlers = new HashMap<CommandType, IMockMessageHandler>();
        this.idHandlers = new HashMap<String, IMockMessageHandler>();
    }

    public void initializeStreams(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void send(Frame frame) {
        wireFormat.marshal((Frame) frame, writer);
    }

    public void send(CharSequence sequence) {
        try {
            writer.append((CharSequence) sequence);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Frame getLastFrame() {
        if(receivedFrames.isEmpty() == false) {
            return receivedFrames.get(receivedFrames.size() - 1);
        } else {
            return null;
        }
    }

    public Frame getLastFrameOfType(CommandType type) {
        Frame frame = null;
        for(int i = this.receivedFrames.size() - 1 ; i >= 0 ; i--) {
            if(this.receivedFrames.get(i).getCommand().equals(type)) {
                frame = this.receivedFrames.get(i);
            }
        }
        return frame;
    }

    public List<Frame> getFrames() {
        return new LinkedList<Frame>(receivedFrames);
    }

    public void processOneReceiveRespondCycle() {
        logger.info("Waiting for frame...");
        Frame frame = wireFormat.unmarshal(reader);
        receivedFrames.add(frame);
        logger.info("Received frame:\n" + frame);
        respond(frame);
    }

    public void respond(Frame frame) {
        logger.info("Responding to frame " + frame.getCommandName());
        Frame response = null;
        IMockMessageHandler handler = null;
        if(StringUtils.isNotBlank(frame.messageId())) {
            handler = idHandlers.get(frame.messageId());
        }
        if(handler == null) {
            handler = typeHandlers.get(frame.getCommand());
        }
        if(handler != null) {
            response = handler.respond(frame);
        }
        if(response != null) {
            send(response);
            logger.info("Server response for frame " + frame.getCommand() + " is " + response.getCommand());
        } else {
            logger.info("No server response for frame " + frame.getCommand());
        }
    }

    public void registerHandler(CommandType commandToHandle, IMockMessageHandler handler) {
        this.typeHandlers.put(commandToHandle, handler);
    }

    public void unregisterHandler(CommandType commandToHandle) {
        this.typeHandlers.remove(commandToHandle);
    }

    public void registerHandler(String messageId, IMockMessageHandler handler) {
        this.idHandlers.put(messageId, handler);
    }

    public void unregisterHandler(String messageId) {
        this.idHandlers.remove(messageId);
    }


    @Override
    public void run() {
        processOneReceiveRespondCycle();
    }
}
