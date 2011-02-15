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

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2010-12-30
 * Time: 12:48 AM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public interface TxConnection extends StompResource, StompOperations {

	void rollback(FrameDecorator frameDecorator) throws StompException;
	
	void rollback() throws StompException;
	
	void commit(FrameDecorator frameDecorator) throws StompException;
	
	void commit() throws StompException;
	
	void setAutoTransactional(boolean transactional) throws StompException;
	
	boolean getAutoTransactional();
	
}
