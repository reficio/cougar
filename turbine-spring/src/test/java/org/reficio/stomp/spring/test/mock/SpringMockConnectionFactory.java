package org.reficio.stomp.spring.test.mock;

import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.MockConnectionFactory;
import org.reficio.stomp.impl.MockConnectionImpl;
import org.reficio.stomp.impl.MockServer;
import org.reficio.stomp.impl.MockTransactionalConnectionImpl;
import org.reficio.stomp.test.mock.IMockMessageHandler;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/21/12
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringMockConnectionFactory<T extends StompResource>  extends MockConnectionFactory<T> {
    public SpringMockConnectionFactory(Class<T> clazz) {
        super(clazz);
    }

    protected T buildConnetion() {
        T resource = super.buildConnetion();
        registerDefaultHandlers(resource);
        return resource;
    }

    private void registerDefaultHandlers(StompResource resource) {
        MockServer server = null;
        if (resource instanceof MockConnectionImpl) {
            server = ((MockConnectionImpl) resource).getServer();
        } else if (resource instanceof MockTransactionalConnectionImpl) {
            server = ((MockTransactionalConnectionImpl) resource).getServer();
        }
        if (server == null) {
            return;
        }
        server.registerHandler(Command.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        server.registerHandler(Command.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(Command.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
    }
}
