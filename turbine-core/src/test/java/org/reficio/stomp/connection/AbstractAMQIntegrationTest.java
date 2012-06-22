package org.reficio.stomp.connection;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.reficio.stomp.core.StompResource;

import static junit.framework.Assert.assertEquals;

/**
 * User: Tom Bujok (tomasz.bujok@centeractive.com)
 * Date: 16/10/11
 * Time: 9:22 PM
 */
public abstract class AbstractAMQIntegrationTest<T extends StompResource> {

    private static BrokerService broker;
    private String stompQueuePrefix = "/queue/";
    private String destinationName = "request";

    protected static final String HOSTNAME = "localhost";
    protected static final int PORT = 61613;

    public abstract T createConnection();

    public String getQueueName() {
        return stompQueuePrefix + destinationName;
    }

    public int getQueueLength() {
        try {
            return broker.getDestination(new ActiveMQQueue(destinationName)).browse().length;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void initialize() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector(String.format("stomp://%s:%d?activemq.prefetchSize=500", HOSTNAME, PORT));
        broker.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        broker.stop();
    }

    @Before
    public void setup() throws Exception {
        broker.getAdminView().addQueue(destinationName);
        assertEquals(0, getQueueLength());
    }

    @After
    public void cleanup() throws Exception {
        broker.getAdminView().removeQueue(destinationName);
    }

}
