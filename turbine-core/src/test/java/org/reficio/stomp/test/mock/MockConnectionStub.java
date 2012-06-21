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

import org.reficio.stomp.impl.MockServer;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-29
 * Time: 11:07 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class MockConnectionStub {

    private PipedInputStream clientInputStream;
    private PipedOutputStream clientOutputStream;
    private PipedInputStream serverInputStream;
    private PipedOutputStream serverOutputStream;

    private Reader serverReader;
    private Writer serverWriter;

    private Reader clientReader;
    private Writer clientWriter;

    private MockServer server;

    private ExecutorService executor;

    public MockConnectionStub() {
        this.server = new MockServer();
        this.executor = Executors.newFixedThreadPool(1);
    }

    public void closeStreams() {
        try {
            this.serverReader.close();
        } catch (IOException e) {
        }
        try {
            this.serverWriter.close();
        } catch (IOException e) {
        }
        try {
            this.clientInputStream.close();
        } catch (IOException e) {
        }
        try {
            this.clientOutputStream.close();
        } catch (IOException e) {
        }
        try {
            this.serverInputStream.close();
        } catch (IOException e) {
        }
        try {
            this.serverOutputStream.close();
        } catch (IOException e) {
        }
    }

    public void initializeStreams(String encoding) {
        try {
            this.clientInputStream = new PipedInputStream(1024 * 1024);
            this.serverInputStream = new PipedInputStream(1024 * 1024);

            this.clientOutputStream = new PipedOutputStream(serverInputStream);
            this.serverOutputStream = new PipedOutputStream(clientInputStream);

            this.clientReader = new InputStreamReader(clientInputStream, encoding);
            this.clientWriter = new OutputStreamWriter(clientOutputStream, encoding);

            this.serverReader = new InputStreamReader(serverInputStream, encoding);
            this.serverWriter = new OutputStreamWriter(serverOutputStream, encoding);

            this.server.initializeStreams(this.serverReader, this.serverWriter);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Writer getMockClientWriter() {
        return this.clientWriter;
    }

    public Reader getMockClientReader() {
        return this.clientReader;
    }

    public void close() {
        // this.server.stop();
    }

    public MockServer getServer() {
        return this.server;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

}