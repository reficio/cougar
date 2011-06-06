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

package org.reficio.stomp.test.unit;

import org.junit.Test;
import org.reficio.stomp.domain.CommandType;

import static org.junit.Assert.*;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-29
 * Time: 12:24 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */

public class CommandTest {

    @Test
    public void getCommand() {
        CommandType command = CommandType.getCommand("CONNECTED");
        assertNotNull(command);
    }

    @Test
    public void getNonExistingCommand() {
        CommandType command = CommandType.getCommand("JAMES_BOND_007");
        assertNull(command);
    }

    @Test
    public void getNullCommand() {
        CommandType command = CommandType.getCommand(null);
        assertNull(command);
    }

    @Test
    public void getClientCommand() {
        CommandType command = CommandType.getCommand("CONNECT");
        assertNotNull(command);
        assertTrue(command.isClientCommand());
    }

    @Test
    public void getServerCommand() {
        CommandType command = CommandType.getCommand("CONNECTED");
        assertNotNull(command);
        assertTrue(command.isServerCommand());
    }



}
