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

    protected String hostname;
    protected Integer port;
    protected String encoding;
    protected Integer timeout;
    protected String username;
    protected String password;
    protected String sessionId;

    // ----------------------------------------------------------------------------------
    // Getters and setters - setters are only for internal usage - parameters VALIDATED
    // ----------------------------------------------------------------------------------
    protected StompResourceImpl hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    protected StompResourceImpl password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    protected StompResourceImpl port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    protected StompResourceImpl timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    protected StompResourceImpl sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    protected StompResourceImpl username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public String getUsername() {
        return username;
    }

    protected StompResourceImpl encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    public abstract void postConstruct();

}
