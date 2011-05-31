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
