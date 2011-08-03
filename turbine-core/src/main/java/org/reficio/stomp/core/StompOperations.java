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

package org.reficio.stomp.core;

import org.reficio.stomp.StompException;
import org.reficio.stomp.domain.Frame;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-11-22
 * Time: 7:54 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public interface StompOperations {

    void send(String destination, FrameDecorator frameDecorator) throws StompException;

    String subscribe(String destination) throws StompException;

    String subscribe(String destination, FrameDecorator frameDecorator) throws StompException;

    String subscribe(String id, String destination) throws StompException;

    String subscribe(String id, String destination, FrameDecorator frameDecorator) throws StompException;

    void unsubscribe(String id) throws StompException;

    void unsubscribe(String id, FrameDecorator frameDecorator) throws StompException;

    Frame receive() throws StompException;

}
