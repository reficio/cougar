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

import org.reficio.stomp.connection.ConnectionFactory;
import org.reficio.stomp.core.StompResource;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-30
 * Time: 12:48 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class TurbineConnectionFactory<T extends StompResource> implements ConnectionFactory<T> {

    protected String hostname;
    protected Integer port;
    protected String encoding;
    protected Integer timeout;
    protected String username;
    protected String password;

    protected final Class clazz;

    public TurbineConnectionFactory(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected TurbineConnectionBuilder.Builder<T> getBuilder() {
        TurbineConnectionBuilder.Builder<T> builder = TurbineConnectionBuilder.<T>builder(clazz);
        return builder;
    }

    protected T buildConnetion() {
        TurbineConnectionBuilder.Builder<T> builder = getBuilder();
        if (hostname != null)
            builder.hostname(hostname);
        if (port != null)
            builder.port(port);
        if (encoding != null)
            builder.encoding(encoding);
        if (timeout != null)
            builder.timeout(timeout);
        if (username != null)
            builder.username(username);
        if (password != null)
            builder.password(password);
        T resource = builder.build();
        return resource;
    }

    @Override
    public T createConnection() {
        T resource = buildConnetion();
        resource.connect();
        return resource;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

}


