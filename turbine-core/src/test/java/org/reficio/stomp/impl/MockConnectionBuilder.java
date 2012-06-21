package org.reficio.stomp.impl;

import org.reficio.stomp.core.FrameValidator;

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

}
