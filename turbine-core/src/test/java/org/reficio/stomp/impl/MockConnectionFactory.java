package org.reficio.stomp.impl;

import org.reficio.stomp.core.StompResource;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/21/12
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockConnectionFactory<T extends StompResource> extends TurbineConnectionFactory<T> {
    public MockConnectionFactory(Class<T> clazz) {
        super(clazz);
    }
    protected TurbineConnectionBuilder.Builder<T> getBuilder() {
        MockConnectionBuilder.Builder<T> builder = MockConnectionBuilder.<T>builder(clazz);
        return builder;
    }
}
