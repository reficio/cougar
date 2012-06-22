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

import org.junit.Test;
import org.reficio.stomp.domain.Header;
import org.reficio.stomp.domain.HeaderType;

import static org.junit.Assert.assertEquals;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-02-09
 * Time: 9:56 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org/
 */
public class HeaderTest {

    @Test
    public void createHeaderByType() {
        Header h = Header.createHeader(HeaderType.CONTENT_LENGTH, "007");
        assertEquals(h.getName(), HeaderType.CONTENT_LENGTH.getName());
        assertEquals(h.getValue(), "007");
    }

    @Test
    public void createHeaderByName() {
        Header h = Header.createHeader("JMS_CORRELATION_ID", "007");
        assertEquals(h.getName(), "JMS_CORRELATION_ID");
        assertEquals(h.getValue(), "007");
    }


}
