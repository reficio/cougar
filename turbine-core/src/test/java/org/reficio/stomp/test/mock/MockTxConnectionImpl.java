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

import org.reficio.stomp.StompException;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ResourceState;
import org.reficio.stomp.impl.TxConnectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-27
 * Time: 03:24 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class MockTxConnectionImpl extends TxConnectionImpl {

    private static final transient Logger log = LoggerFactory.getLogger(MockTxConnectionImpl.class);

    private MockConnectionStub stub;

    public MockTxConnectionImpl() {
        super();
        this.stub = new MockConnectionStub();
    }

    @Override
    protected void initializeSocket(int timeout) {
        this.socket = new Socket();
    }

    @Override
    protected void closeSocket() {
        super.closeSocket();
    }


    @Override
    protected void closeStreams() {
        super.closeStreams();
        this.stub.closeStreams();
    }

    @Override
    protected void initializeStreams(int timeout) {
        this.stub.initializeStreams(super.getEncoding());
        this.reader = this.stub.getMockClientReader();
        this.writer = this.stub.getMockClientWriter();
    }

    @Override
	public synchronized void close() {
		assertOperational();
		log.info(String.format("Closing connection=[%s]", this));
        setState(ResourceState.CLOSING);
		disconnect();
        unmarshall();
        this.stub.close();
        closeCommunication();
        setState(ResourceState.CLOSED);
	}

    @Override
	protected void marshall(Frame frame) throws StompException {
        stub.getExecutor().submit(getServer());
		log.info("Sending frame: \n" + frame);
        try {
		    wireFormat.marshal(frame, writer);
        } catch(RuntimeException ex) {
            setState(ResourceState.ERROR);
            throw ex;
        }
    }

    public MockConnectionStub getStub() {
        return this.stub;
    }

    public MockServer getServer() {
        return this.stub.getServer();
    }

}
