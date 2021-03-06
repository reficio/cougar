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

package org.reficio.cougar.impl;

import org.reficio.cougar.core.FramePreprocessor;
import org.reficio.cougar.core.StompResourceState;
import org.reficio.cougar.core.StompWireFormat;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-27
 * Time: 03:23 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class MockClientImpl extends ClientImpl {

    private MockTransmissionHandler mockTransmissionHandler;

    protected MockClientImpl(StompWireFormat wireFormat, FramePreprocessor preprocessor) {
        super(wireFormat, preprocessor);
        this.mockTransmissionHandler = new MockTransmissionHandler(wireFormat);
    }

    public void postConstruct() {
        this.setTransmissionHandler(mockTransmissionHandler);
    }

    public MockConnectionStub getStub() {
        return mockTransmissionHandler.getStub();
    }

    public MockServer getServer() {
        return mockTransmissionHandler.getStub().getServer();
    }

    @Override
    public synchronized void close() {
        assertOperational();
        setState(StompResourceState.CLOSING);
        disconnect();
        mockTransmissionHandler.unmarshall();
        mockTransmissionHandler.getStub().close();
        setState(StompResourceState.CLOSED);
    }

}