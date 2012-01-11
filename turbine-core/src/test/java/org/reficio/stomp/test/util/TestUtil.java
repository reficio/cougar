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
package org.reficio.stomp.test.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-08-29
 * Time: 9:36 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class TestUtil {

    public static int getFreePort() {
        try {
            ServerSocket localmachine = new ServerSocket(0);
            localmachine.close();
            int port = localmachine.getLocalPort();
            return port;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
