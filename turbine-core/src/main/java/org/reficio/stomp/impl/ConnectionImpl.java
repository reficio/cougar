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
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.util.SubscriptionRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ConnectionImpl extends ClientImpl implements Connection {

	private static final transient Logger log = LoggerFactory.getLogger(ConnectionImpl.class);

    // private SubscriptionRegister register;

	public ConnectionImpl() {
		super();
	}

    @Override
    protected void doSetAttributes(String hostname, int port, String username, String password, String encoding) {
        super.doSetAttributes(hostname, port, username, password, encoding);
        // this.register = new SubscriptionRegister();
        this.wireFormat = new WireFormatImpl(/*this.register*/);
    }

	@Override
	public void abort(String transactionId, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.ABORT);
		frame.transaction(transactionId);
		preprocessor.decorate(frame, frameDecorator);
		send(frame);
	}

	@Override
	public void abort(String transactionId) {		
		abort(transactionId, emptyDecorator);
	}	

	@Override
	public void ack(String messageId, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.ACK);
        frame.messageId(messageId);
        preprocessor.decorate(frame, frameDecorator);
		send(frame);
	}

	@Override
	public void ack(String messageId) {
		ack(messageId, emptyDecorator);
	}

	@Override
	public void begin(String transactionId, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.BEGIN);
		frame.transaction(transactionId);
		preprocessor.decorate(frame, frameDecorator);
		send(frame);
	}

	@Override
	public void begin(String transactionId) {
		begin(transactionId, emptyDecorator);
	}

	@Override
	public void commit(String transactionId, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.COMMIT);
		frame.transaction(transactionId);
		preprocessor.decorate(frame, frameDecorator);
		send(frame);
	}

	@Override
	public void commit(String transactionId) {
		commit(transactionId, emptyDecorator);
	}

	@Override
	public void send(String destination, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.SEND);
        frame.destination(destination);
		preprocessor.decorate(frame, frameDecorator);
		send(frame);
	}

    @Override
    public String subscribe(String destination, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.SUBSCRIBE);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        String subscriptionId = frame.subscriptionId();
        if (subscriptionId == null) {
            subscriptionId = UUID.randomUUID().toString(); // register.subscribe(null);
        }
        frame.subscriptionId(subscriptionId);
        try {
            send(frame);
            return subscriptionId;
        } catch (RuntimeException ex) {
            // register.unsubscribe(subscriptionId);
            throw ex;
        }
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
        Frame frame = new Frame(CommandType.SUBSCRIBE);
        frame.destination(destination);
        frame.subscriptionId(id);
		preprocessor.decorate(frame, frameDecorator);
        // try {
        //     register.subscribe(id);
		    send(frame);
        // } catch (RuntimeException ex) {
        //     register.unsubscribe(id);
        //    throw ex;
        // }
        return id;
    }

    @Override
	public void unsubscribe(String id) {
		unsubscribe(id, emptyDecorator);
	}

	@Override
	public void unsubscribe(String id, FrameDecorator frameDecorator) {
		Frame frame = new Frame(CommandType.UNSUBSCRIBE);
        frame.subscriptionId(id);
		preprocessor.decorate(frame, frameDecorator);
        // register.unsubscribe(id);
		send(frame);
	}

	protected FrameDecorator emptyDecorator = new FrameDecorator() {
		@Override
		public void decorateFrame(Frame frame) {
		}
	};

}
