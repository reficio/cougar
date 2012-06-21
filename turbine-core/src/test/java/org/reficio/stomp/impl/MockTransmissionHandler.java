package org.reficio.stomp.impl;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.StompEncodingException;
import org.reficio.stomp.StompException;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.core.TransmissionHandler;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.test.mock.MockConnectionStub;

import java.io.*;
import java.net.InetSocketAddress;
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