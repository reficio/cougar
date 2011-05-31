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
package org.reficio.stomp.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.spring.service.TransactionalOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext-test.xml"})
public class InitTest {

	private static final Log logger = LogFactory.getLog(InitTest.class);

    @Autowired
    @Qualifier("stompTemplate")
	private StompTemplate stompTemplate;

    @Autowired
    @Qualifier("transactionalStompTemplate")
	private StompTemplate transactionalStompTemplate;
	
	@Autowired
	private TransactionalOrchestrator transactionalOrchestrator;
	
	@Test
	public void init() {
		logger.info(stompTemplate);
	}
	
	@Test
	public void send() {
		FrameDecorator decorator = new FrameDecorator() {
			@Override
			public void decorateFrame(Frame frame) {
				// frame.destination(UUID.randomUUID().toString());
				frame.payload("My wife is the best wife in the world.");
				return;
			}
		};
		stompTemplate.send("queue/r", decorator);
	}
	
	@Test
	public void sendInLocalTransaction() {
		FrameDecorator decorator = new FrameDecorator() {
			@Override
			public void decorateFrame(Frame frame) {
				// frame.destination(UUID.randomUUID().toString());
				frame.payload("My wife is the best wife in the world.");
				return;
			}
		};
		transactionalStompTemplate.send("queue/r", decorator);
	}
	
	
	@Test
	public void sendInTransaction() {
		transactionalOrchestrator.sendOrchestrate();
	}
	
}
