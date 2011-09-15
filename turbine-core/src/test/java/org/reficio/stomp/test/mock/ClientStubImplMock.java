package org.reficio.stomp.test.mock;

import org.reficio.stomp.connection.Client;
import org.reficio.stomp.core.StompWireFormat;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.stub.ClientStubImpl;

import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 15/09/11
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientStubImplMock extends ClientStubImpl<Client> {
    @Override
    public void init() {
        this.socket = new Socket();
    }

    public void marshallPublic(Frame frame) {
        super.marshall(frame);
    }

    public void unmarshallPublic() {
        super.unmarshall();
    }

    @Override
    protected StompWireFormat createWireFormat() {
        return new StompWireFormat() {

            @Override
            public void marshal(Frame frame, Writer writer) {
                throw new RuntimeException();
            }

            @Override
            public Frame unmarshal(Reader in) {
                throw new RuntimeException();
            }

            @Override
            public String getVersion() {
                throw new RuntimeException();
            }
        };
    }
}
