package org.reficio.stomp.test.mock;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.StompResource;


public class PrivateConstructorResource implements StompResource {
    private PrivateConstructorResource() {
    }

    @Override
    public void init(String hostname, int port, String username, String password, String encoding) throws StompException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(String hostname, int port, String username, String password, String encoding, int timeout) throws StompException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws StompException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHostname() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPassword() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getUsername() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSessionId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEncoding() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInitialized() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}