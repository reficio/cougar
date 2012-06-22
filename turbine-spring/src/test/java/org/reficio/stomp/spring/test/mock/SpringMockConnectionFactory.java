package org.reficio.stomp.spring.test.mock;

import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.*;
import org.reficio.stomp.impl.IMockMessageHandler;

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
        if (resource instanceof MockClientImpl) {
            server = ((MockClientImpl) resource).getServer();
        } else if (resource instanceof MockTransactionalClientImpl) {
            server = ((MockTransactionalClientImpl) resource).getServer();
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
