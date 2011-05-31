package org.reficio.stomp.spring.mock;

import org.reficio.stomp.StompConnectionException;
import org.reficio.stomp.connection.StompConnectionFactory;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.test.mock.IMockMessageHandler;
import org.reficio.stomp.test.mock.MockConnectionImpl;
import org.reficio.stomp.test.mock.MockServer;
import org.reficio.stomp.test.mock.MockTxConnectionImpl;

import java.util.UUID;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class MockConnectionFactory<T extends StompResource> extends StompConnectionFactory {

    public MockConnectionFactory(Class clazz) {
        super(clazz);
    }

    @Override
    public T createConnection() {
        try {
            T connection = (T) clazz.newInstance();
            registerHandlers(connection);
            connection.init(hostname, port, username, password, encoding);
            return connection;
        } catch (InstantiationException e) {
            throw new StompConnectionException("Error during the creation of a new connection", e);
        } catch (IllegalAccessException e) {
            throw new StompConnectionException("Error during the creation of a new connection", e);
        }
    }

    private void registerHandlers(StompResource resource) {

        MockServer server = null;
        if (resource instanceof MockConnectionImpl) {
            server = ((MockConnectionImpl) resource).getServer();
        } else if (resource instanceof MockTxConnectionImpl) {
            server = ((MockTxConnectionImpl) resource).getServer();
        }
        if (server == null) {
            return;
        }
        server.registerHandler(CommandType.CONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(CommandType.CONNECTED);
                response.session(UUID.randomUUID().toString());
                return response;
            }
        });

        server.registerHandler(CommandType.DISCONNECT, new IMockMessageHandler() {
            @Override
            public Frame respond(Frame request) {
                Frame response = new Frame(CommandType.RECEIPT);
                response.receiptId(request.messageId());
                return response;
            }
        });
    }

}
