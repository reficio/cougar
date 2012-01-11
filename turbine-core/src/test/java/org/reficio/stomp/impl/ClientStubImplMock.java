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

import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.stub.ClientStubImpl;

import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-09-15
 * Time: 10:32 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ClientStubImplMock extends ClientImpl {

    @Override
    public void init() {
    }

    public void marshallPublic(Frame frame) {
        super.marshall(frame);
    }

    public void unmarshallPublic() {
        super.unmarshall();
    }

//    @Override
//    protected StompWireFormat createWireFormat() {
//        return new StompWireFormat() {
//
//            @Override
//            public void marshal(Frame frame, Writer writer) {
//                throw new RuntimeException();
//            }
//
//            @Override
//            public Frame unmarshal(Reader in) {
//                throw new RuntimeException();
//            }
//
//            @Override
//            public String getVersion() {
//                throw new RuntimeException();
//            }
//        };
//    }
}
