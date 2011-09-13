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

package org.reficio.stomp.domain;

import org.apache.commons.lang.StringUtils;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public enum CommandType {

    CONNECT(true),
    DISCONNECT(true),

    SUBSCRIBE(true),
    UNSUBSCRIBE(true),

    BEGIN(true),
    COMMIT(true),
    ABORT(true),
    ACK(true),

    SEND(true),

    CONNECTED(false),
    MESSAGE(false),
    RECEIPT(false),
    ERROR(false);

    private final boolean clientCommand;

    private CommandType(boolean clientCommand) {
        this.clientCommand = clientCommand;
    }

    public static CommandType getCommand(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Enum.valueOf(CommandType.class, value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public boolean isClientCommand() {
        return clientCommand;
    }

    public boolean isServerCommand() {
        return clientCommand == false;
    }

    public String getName() {
		return name();
	}

    @Override
    public String toString() {
        return this.name();
    }

}
