package org.reficio.stomp.impl;

import org.apache.activemq.protobuf.WireFormat;
import org.reficio.stomp.core.StompResourceState;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.test.mock.MockConnectionStub;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/20/12
 * Time: 2:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockClientImpl extends ClientImpl {
    private MockTransmissionHandler mockTransmissionHandler;

    protected MockClientImpl() {
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
