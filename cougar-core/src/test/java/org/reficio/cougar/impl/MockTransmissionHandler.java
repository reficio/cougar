package org.reficio.cougar.impl;

import org.reficio.cougar.StompException;
import org.reficio.cougar.core.StompWireFormat;
import org.reficio.cougar.domain.Frame;

import java.net.Socket;

/**
 * User: Tom Bujok (tomasz.bujok@centeractive.com)
 * Date: 16/10/11
 * Time: 10:08 PM
 */
public class MockTransmissionHandler extends TransmissionHandlerImpl {

    private MockConnectionStub stub;

    MockTransmissionHandler(StompWireFormat wireFormat) {
        super(wireFormat, "localhost", 61616, "UTF-8");
        this.socket = new Socket();
        this.stub = new MockConnectionStub();
    }

    protected void initializeSocket(int timeout) {
        this.socket = new Socket();
    }

    protected void initializeStreams(int timeout) {
        stub.initializeStreams("UTF-8");
        this.reader = stub.getMockClientReader();
        this.writer = stub.getMockClientWriter();
    }

    public void marshall(Frame frame) throws StompException {
        stub.getExecutor().submit(stub.getServer());
        super.marshall(frame);
    }

    public MockConnectionStub getStub() {
        return this.stub;
    }

}
