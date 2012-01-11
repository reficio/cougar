package org.reficio.stomp.impl;

import org.reficio.stomp.connection.Client;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.FrameValidator;
import org.reficio.stomp.core.StompResource;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 09/10/11
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Turbine {

    public interface Builder<T extends StompResource> {
        Builder<T> hostname(String hostname);
        Builder<T> port(int port);
        Builder<T> username(String username);
        Builder<T> password(String password);
        Builder<T> encoding(String encoding);
        Builder<T> timeout(int timeout);
        T build();
        T buildAndInit();
    }

    public static abstract class AbstractBuilder<T extends StompResource> implements Builder<T> {
        private String hostname;
        private int port;
        private String encoding;
        private int timeout;
        private String username;
        private String password;

        // ----------------------------------------------------------------------------------
        // Builder methods - parameters VALIDATED
        // ----------------------------------------------------------------------------------
        public Builder<T> hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder<T> port(int port) {
            this.port = port;
            return this;
        }

        public Builder<T> username(String username) {
            this.username = username;
            return this;
        }

        public Builder<T> password(String password) {
            this.password = password;
            return this;
        }

        public Builder<T> encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public Builder<T> timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public abstract T build();

        public T buildAndInit() {
            T resource = build();
            resource.init();
            return resource;
        }
    }

    public static Builder<Client> client() {
        return new AbstractBuilder<Client>() {
            @Override
            public Client build() {
                return new ClientImpl(new WireFormatImpl());
            }
        };
    }

    public static Builder<Connection> connection() {
        return new AbstractBuilder<Connection>() {
            @Override
            public Connection build() {
                return new ConnectionImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

    public static Builder<TransactionalConnection> transactionalConnection() {
        return new AbstractBuilder<TransactionalConnection>() {
            @Override
            public TransactionalConnection build() {
                return new TransactionalConnectionImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

}