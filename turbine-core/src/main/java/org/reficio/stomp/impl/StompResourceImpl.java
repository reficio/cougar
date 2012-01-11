package org.reficio.stomp.impl;

import org.reficio.stomp.core.StompResource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 09/10/11
 * Time: 1:22 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class StompResourceImpl implements StompResource {

    protected String hostname = DEFAULT_HOSTNAME;
    protected int port = DEFAULT_PORT;
    protected String encoding = DEFAULT_ENCODING;
    protected int timeout = DEFAULT_TIMEOUT_IN_MILLIS;
    protected String username;
    protected String password;
    protected String sessionId;

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 61613;
    public static final int DEFAULT_TIMEOUT_IN_MILLIS = 1000;

    // ----------------------------------------------------------------------------------
    // Getters and setters - setters are only for internal usage - parameters VALIDATED
    // ----------------------------------------------------------------------------------
    protected void setHostname(String hostname) {
        this.hostname = checkNotNull(hostname, "hostname cannot be null");
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    protected void setPassword(String password) {
        this.password = checkNotNull(password, "password cannot be null");
    }

    @Override
    public String getPassword() {
        return password;
    }

    protected void setPort(int port) {
        checkArgument(port > 0, "port must be positive");
        this.port = port;
    }

    @Override
    public int getPort() {
        return port;
    }

    protected void setTimeout(int timeout) {
        checkArgument(timeout >= 0, "timeout must be positive or zero");
        this.timeout = timeout;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    protected void setSessionId(String sessionId) {
        this.sessionId = checkNotNull(sessionId, "sessionId cannot be null");
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    protected void setUsername(String username) {
        this.username = checkNotNull(username, "username cannot be null");
    }

    @Override
    public String getUsername() {
        return username;
    }

    protected void setEncoding(String encoding) {
        this.encoding = checkNotNull(encoding, "encoding cannot be null");
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

}
