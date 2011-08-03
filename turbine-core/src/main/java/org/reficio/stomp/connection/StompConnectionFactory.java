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

package org.reficio.stomp.connection;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.core.StompResource;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-30
 * Time: 12:48 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class StompConnectionFactory<T extends StompResource> implements ConnectionFactory<T> {

	protected String hostname;
	protected int port;	
	protected String username;
	protected String password;
	protected String encoding;		
	
	protected final Class clazz;
	
	public StompConnectionFactory(Class<? extends T> clazz) {
		this.clazz = clazz;
	}
	
	@Override
	public T createConnection() {
        try {
            T connection = (T)clazz.newInstance();
            connection.init(hostname, port, username, password, encoding);
            return connection;
        } catch (InstantiationException e) {
            throw new StompConnectionException("Error during the creation of a new connection", e);
        } catch (IllegalAccessException e) {
            throw new StompConnectionException("Error during the creation of a new connection", e);
        }
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

	public int getPort() {
		return port;
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
	
	public void setPort(int port) {
		this.port = port;
	}
	
}

