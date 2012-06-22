package org.reficio.stomp.impl;

import org.reficio.stomp.StompException;
import org.reficio.stomp.connection.Client;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.StompResource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 09/10/11
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionBuilder {

    public interface Builder<T extends StompResource> {
        Builder<T> hostname(String hostname);
        Builder<T> port(Integer port);
        Builder<T> username(String username);
        Builder<T> password(String password);
        Builder<T> encoding(String encoding);
        Builder<T> timeout(Integer timeout);
        T build();
        T buildAndConnect();
    }

    public static abstract class AbstractBuilder<T extends StompResource> implements Builder<T> {
        private String hostname = DEFAULT_HOSTNAME;
        private Integer port = DEFAULT_PORT;
        private String encoding = DEFAULT_ENCODING;
        private Integer timeout = DEFAULT_TIMEOUT_IN_MILLIS;
        private String username;
        private String password;

        private static final String DEFAULT_ENCODING = "UTF-8";
        private static final String DEFAULT_HOSTNAME = "localhost";
        private static final int DEFAULT_PORT = 61613;
        private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10000;

        // ----------------------------------------------------------------------------------
        // Builder methods - parameters VALIDATED
        // ----------------------------------------------------------------------------------
        public Builder<T> hostname(String hostname) {
            this.hostname = checkNotNull(hostname, "hostname cannot be null");
            return this;
        }

        public Builder<T> port(Integer port) {
            checkArgument(port > 0, "port must be positive");
            this.port = port;
            return this;
        }

        public Builder<T> username(String username) {
            this.username = checkNotNull(username, "password cannot be null");
            return this;
        }

        public Builder<T> password(String password) {
            this.password = checkNotNull(password, "password cannot be null");
            return this;
        }

        public Builder<T> encoding(String encoding) {
            this.encoding = checkNotNull(encoding, "encoding cannot be null");
            return this;
        }

        public Builder<T> timeout(Integer timeout) {
            checkArgument(timeout >= 0, "timeout must not be negative");
            this.timeout = timeout;
            return this;
        }

        public abstract T instantiate();

        public T build() {
            T resource = instantiate();
            populateFields(resource);
            return resource;
        }

        public T buildAndConnect() {
            T resource = build();
            resource.connect();
            return resource;
        }

        private void populateFields(T resource) {
            StompResourceImpl impl = (StompResourceImpl) resource;
            impl.hostname(hostname);
            impl.port(port);
            impl.encoding(encoding);
            impl.timeout(timeout);
            if (username != null) {
                impl.username(username);
            }
            if (password != null) {
                impl.password(password);
            }
            impl.postConstruct();
        }

    }

    public static Builder<Client> client() {
        return new AbstractBuilder<Client>() {
            @Override
            public Client instantiate() {
                return new ClientImpl(new WireFormatImpl());
            }
        };
    }

    public static Builder<Connection> connection() {
        return new AbstractBuilder<Connection>() {
            @Override
            public Connection instantiate() {
                return new ConnectionImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

    public static Builder<TransactionalConnection> transactionalConnection() {
        return new AbstractBuilder<TransactionalConnection>() {
            @Override
            public TransactionalConnection instantiate() {
                return new TransactionalConnectionImpl(new WireFormatImpl(), new FrameValidator());
            }
        };
    }

    @SuppressWarnings("unchecked") // impossible to omit
    public static <T extends StompResource> Builder<T> builder(Class<T> clazz) {
        if (clazz.equals(Client.class)) {
            return (Builder<T>) client();
        } else if (clazz.equals(Connection.class)) {
            return (Builder<T>) connection();
        } else if (clazz.equals(TransactionalConnection.class)) {
            return (Builder<T>) transactionalConnection();
        }
        throw new StompException(clazz.getName() + " is not supported");
    }

}