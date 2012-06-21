package org.reficio.stomp.impl;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.FrameValidator;
import org.reficio.stomp.core.StompResource;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/20/12
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockConnectionBuilder extends TurbineConnectionBuilder {

    public static Builder<MockClientImpl> mockClient() {
        return new AbstractBuilder<MockClientImpl>() {
            @Override
            public MockClientImpl instantiate() {
                return new MockClientImpl();
            }
        };
    }

    public static Builder<MockConnectionImpl> mockConnection() {
        return new AbstractBuilder<MockConnectionImpl>() {
            @Override
            public MockConnectionImpl instantiate() {
                return new MockConnectionImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

    public static Builder<MockTransactionalConnectionImpl> mockTransactionalConnection() {
        return new AbstractBuilder<MockTransactionalConnectionImpl>() {
            @Override
            public MockTransactionalConnectionImpl instantiate() {
                return new MockTransactionalConnectionImpl();
            }
        };
    }

    @SuppressWarnings("unchecked") // impossible to omit
    static <T extends StompResource> Builder<T> builder(Class<T> clazz) {
        if (clazz.equals(Client.class)) {
            return (Builder<T>) mockClient();
        } else if (clazz.equals(Connection.class)) {
            return (Builder<T>) mockConnection();
        } else if (clazz.equals(TransactionalConnection.class)) {
            return (Builder<T>) mockTransactionalConnection();
        }
        throw new StompException(clazz.getName() + " is not supported");
    }

}
