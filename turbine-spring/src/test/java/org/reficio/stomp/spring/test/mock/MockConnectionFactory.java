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
package org.reficio.stomp.spring.test.mock;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.impl.MockConnectionImpl;
import org.reficio.stomp.impl.MockServer;
import org.reficio.stomp.impl.MockTransactionalConnectionImpl;
import org.reficio.stomp.impl.TurbineConnectionFactory;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.test.mock.IMockMessageHandler;

import java.util.UUID;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class MockConnectionFactory<T extends StompResource> extends TurbineConnectionFactory<T> {

    public MockConnectionFactory(Class clazz) {
        super(clazz);
    }

    @Override
    public T createConnection() {
        T resource = buildConnetion();
        resource.connect();
        return resource;
    }

    private void registerHandlers(StompResource resource) {
        MockServer server = null;
        if (resource instanceof MockConnectionImpl) {
            server = ((MockConnectionImpl) resource).getServer();
        } else if (resource instanceof MockTransactionalConnectionImpl) {
            server = ((MockTransactionalConnectionImpl) resource).getServer();
        }
        if (server == null) {
            return;
        }
        server.registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        server.registerHandler(Command.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
    }

}
