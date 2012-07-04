package org.reficio.cougar.impl;

import org.reficio.cougar.core.StompResource;
import org.reficio.cougar.factory.SimpleConnectionFactory;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/21/12
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockConnectionFactory<T extends StompResource> extends SimpleConnectionFactory<T> {
    public MockConnectionFactory(Class<T> clazz) {
        super(clazz);
    }
    protected ConnectionBuilder.Builder<T> getBuilder() {
        MockConnectionBuilder.Builder<T> builder = MockConnectionBuilder.<T>builder(clazz);
        return builder;
    }
}
