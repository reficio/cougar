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

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.StompResource;


public class PrivateConstructorResource implements StompResource {
    private PrivateConstructorResource() {
    }

    @Override
    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws StompException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource hostname(String hostname) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource port(int port) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource username(String username) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource password(String password) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource encoding(String encoding) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StompResource timeout(int timeout) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHostname() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPassword() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getUsername() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSessionId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEncoding() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInitialized() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
