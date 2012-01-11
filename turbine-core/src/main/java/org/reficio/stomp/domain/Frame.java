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

import org.reficio.stomp.core.FrameBuilder;

import java.util.Map;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class Frame extends FrameBuilder implements Cloneable {

    public Frame(Command command, Map<String, Header> headers, String payload) {
        super(command, headers, payload);
    }

    public Frame(Command command) {
        super(command);
    }

    public Frame(Command command, boolean validationEnabled) {
        super(command, validationEnabled);
    }

    public Frame(String commandName) {
        super(commandName);
    }

    public Frame(String commandName, boolean validationEnabled) {
        super(commandName, validationEnabled);
    }

    public boolean indicatesError() {
        return command.equals(Command.ERROR.getName());
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Command:\t[%s]\n", command));
        builder.append("Headers:\n");
        for (Header header : headers.values()) {
            builder.append(String.format("  %s\n", header));
        }
        builder.append(String.format("Payload:\t[%s]", payload != null ? payload : ""));
        return builder.toString();
    }

}
