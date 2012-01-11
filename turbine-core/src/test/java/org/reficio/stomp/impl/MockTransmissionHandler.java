package org.reficio.stomp.impl;

import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.core.TransmissionHandler;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.test.mock.MockConnectionStub;

/**
 * User: Tom Bujok (tomasz.bujok@centeractive.com)
 * Date: 16/10/11
 * Time: 10:08 PM
 */
public class MockTransmissionHandler extends TransmissionHandlerImpl {

    private MockConnectionStub stub;

    MockTransmissionHandler(StompWireFormat wireFormat, String hostname, int port, String encoding) {
        super(wireFormat, hostname, port, encoding);
        this.reader = stub.getMockClientReader();
        this.writer = stub.getMockClientWriter();
    }

}
