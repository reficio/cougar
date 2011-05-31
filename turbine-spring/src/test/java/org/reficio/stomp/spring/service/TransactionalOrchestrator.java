package org.reficio.stomp.spring.service;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalOrchestrator {

	TransactionalOperations top;
	
	@Transactional(readOnly = false)
	public void sendOrchestrate() {
		top.sendRequired();
		top.sendRequiresNew();
		top.sendRequiredToo();
	}

	public TransactionalOperations getTop() {
		return top;
	}

	public void setTop(TransactionalOperations top) {
		this.top = top;
	}
	
	
}
