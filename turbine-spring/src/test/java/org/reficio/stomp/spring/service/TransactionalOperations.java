package org.reficio.stomp.spring.service;

import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.spring.StompTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalOperations {

	private StompTemplate stompTemplate;

	@Transactional(readOnly = false)
	public void sendRequired() {
		stompTemplate.send("R/TEST/REQUIRED", emptyDecorator);
	}
	
	@Transactional(readOnly = false)
	public void sendRequiredToo() {
		stompTemplate.send("R/TEST/REQUIRED_TOO", emptyDecorator);
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void sendRequiresNew() {
		stompTemplate.send("R/TEST/REQUIRES_NEW", emptyDecorator);
	}
	
	private FrameDecorator emptyDecorator = new FrameDecorator() {
		@Override
		public void decorateFrame(Frame frame) {
			return;
		}
	};

	public StompTemplate getStompTemplate() {
		return stompTemplate;
	}

	public void setStompTemplate(StompTemplate stompTemplate) {
		this.stompTemplate = stompTemplate;
	}



}
