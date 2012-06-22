package org.reficio.stomp.impl;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.connection.TransactionalClient;
import org.reficio.stomp.core.StompResource;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/20/12
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockConnectionBuilder extends ConnectionBuilder {

    public static Builder<MockConnectionImpl> mockClient() {
        return new AbstractBuilder<MockConnectionImpl>() {
            @Override
            public MockConnectionImpl instantiate() {
                return new MockConnectionImpl();
            }
        };
    }

    public static Builder<MockClientImpl> mockConnection() {
        return new AbstractBuilder<MockClientImpl>() {
            @Override
            public MockClientImpl instantiate() {
                return new MockClientImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

    public static Builder<MockTransactionalClientImpl> mockTransactionalConnection() {
        return new AbstractBuilder<MockTransactionalClientImpl>() {
            @Override
            public MockTransactionalClientImpl instantiate() {
                return new MockTransactionalClientImpl();
            }
        };
    }

    @SuppressWarnings("unchecked") // impossible to omit
    public static <T extends StompResource> Builder<T> builder(Class<T> clazz) {
        if (clazz.equals(Connection.class)) {
            return (Builder<T>) mockClient();
        } else if (clazz.equals(Client.class)) {
            return (Builder<T>) mockConnection();
        } else if (clazz.equals(TransactionalClient.class)) {
            return (Builder<T>) mockTransactionalConnection();
        }
        throw new StompException(clazz.getName() + " is not supported");
    }

}
