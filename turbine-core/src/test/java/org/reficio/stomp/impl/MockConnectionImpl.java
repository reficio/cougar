package org.reficio.stomp.impl;

import org.reficio.stomp.core.StompResourceState;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/20/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockConnectionImpl extends ConnectionImpl {
    private MockTransmissionHandler mockTransmissionHandler;

    protected MockConnectionImpl() {
        super(new WireFormatImpl());
        this.mockTransmissionHandler = new MockTransmissionHandler(new WireFormatImpl());
    }

    public void postConstruct() {
        this.setTransmissionHandler(mockTransmissionHandler);
    }

    public MockConnectionStub getStub() {
        return mockTransmissionHandler.getStub();
    }

    public MockServer getServer() {
        return mockTransmissionHandler.getStub().getServer();
    }

    @Override
    public synchronized void close() {
        assertOperational();
        setState(StompResourceState.CLOSING);
        disconnect();
        mockTransmissionHandler.unmarshall();
        mockTransmissionHandler.getStub().close();
        setState(StompResourceState.CLOSED);
    }
}
