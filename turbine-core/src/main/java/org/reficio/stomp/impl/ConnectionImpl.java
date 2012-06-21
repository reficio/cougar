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

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.core.FramePreprocessor;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.reficio.stomp.core.StompResourceState.NEW;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
class ConnectionImpl extends ClientImpl implements Connection {

    private static final transient Logger log = LoggerFactory.getLogger(ConnectionImpl.class);

    protected FramePreprocessor preprocessor;

    ConnectionImpl(StompWireFormat wireFormat, FramePreprocessor preprocessor) {
        super(wireFormat);
        this.preprocessor = preprocessor;
    }

    public void postConstruct() {
        super.postConstruct();
    }

    public void abort(String transactionId, FrameDecorator frameDecorator) {
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void abort(String transactionId) {
        abort(transactionId, emptyDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        checkNotNull(messageId, "messageId cannot be null");
        Frame frame = new Frame(Command.ACK);
        frame.messageId(messageId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public void ack(String messageId) {
        ack(messageId, emptyDecorator);
    }

    public void begin(String transactionId, FrameDecorator frameDecorator) {
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.BEGIN);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void begin(String transactionId) {
        begin(transactionId, emptyDecorator);
    }

    public void commit(String transactionId, FrameDecorator frameDecorator) {
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void commit(String transactionId) {
        commit(transactionId, emptyDecorator);
    }

    @Override
    public void send(String destination, FrameDecorator frameDecorator) {
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SEND);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public String subscribe(String destination, FrameDecorator frameDecorator) {
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SUBSCRIBE);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        String subscriptionId = frame.subscriptionId();
        if (subscriptionId == null) {
            subscriptionId = UUID.randomUUID().toString();
        }
        frame.subscriptionId(subscriptionId);
        send(frame);
        return subscriptionId;
    }

    @Override
    public String subscribe(String destination) throws StompException {
        return subscribe(destination, emptyDecorator);
    }

    @Override
    public String subscribe(String id, String destination) throws StompException {
        return subscribe(id, destination, emptyDecorator);
    }

    @Override
    public String subscribe(String id, String destination, FrameDecorator frameDecorator) throws StompException {
        checkNotNull(id, "id cannot be null");
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SUBSCRIBE);
        frame.destination(destination);
        frame.subscriptionId(id);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
        return id;
    }

    @Override
    public void unsubscribe(String id) {
        unsubscribe(id, emptyDecorator);
    }

    @Override
    public void unsubscribe(String id, FrameDecorator frameDecorator) {
        checkNotNull(id, "id cannot be null");
        Frame frame = new Frame(Command.UNSUBSCRIBE);
        frame.subscriptionId(id);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    protected FrameDecorator emptyDecorator = new FrameDecorator() {
        @Override
        public void decorateFrame(Frame frame) {
        }
    };

}
