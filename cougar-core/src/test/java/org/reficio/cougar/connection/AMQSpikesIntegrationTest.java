package org.reficio.cougar.connection;

import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.junit.Ignore;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: 6/22/12
 * Time: 9:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Ignore
public class AMQSpikesIntegrationTest extends AbstractAMQIntegrationTest<Client> {

    // ActiveMQ prefetchSize header works only in manual ACK mode!!!!!
    public int checkPrefetchSizeReception(int messagesCount, String ackMode) throws Exception {
        String queue = "/queue/spikes" + System.currentTimeMillis();

        StompConnection sender = new StompConnection();
        sender.open("localhost", 61613);
        sender.connect("user", "password");
        for (int i = 0; i < messagesCount; i++) {
            sender.send(queue, "David Hasselhoff is cool");
        }
        sender.disconnect();

        StompConnection connection = new StompConnection();
        connection.open("localhost", 61613);
        connection.connect("user", "password");

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Stomp.Headers.Send.PERSISTENT, "true");
        map.put("activemq.prefetchSize", "1");
        map.put("activemq.dispatchAsync", "false");
        connection.subscribe(queue, ackMode, map);

        StompFrame frame = connection.receive();
        assertNotNull(frame);
        if (ackMode.equalsIgnoreCase("AUTO") == false) {
            connection.ack(frame);
        }
        connection.unsubscribe(queue);

        int receivedAfterUnsubscribe = 0;
        try {
            for (int i = 0; i < messagesCount; i++) {
                StompFrame afterUnsubscribe = connection.receive(1500);
                if (afterUnsubscribe != null) {
                    receivedAfterUnsubscribe++;
                }
            }
        } catch (SocketTimeoutException ex) {
            // ignore
        }
        connection.disconnect();
        return receivedAfterUnsubscribe;
    }

    @Test
    public void test_AutoAck() throws Exception {
        int messagesCount = 1000;
        int receivedAfterUnsubscribe = checkPrefetchSizeReception(messagesCount, Stomp.Headers.Subscribe.AckModeValues.AUTO);
        assertTrue("receivedAfterUnsubscribe was " + receivedAfterUnsubscribe, receivedAfterUnsubscribe > 1);
    }

    @Test
    public void test_ClientAck() throws Exception {
        int messagesCount = 1000;
        int receivedAfterUnsubscribe = checkPrefetchSizeReception(messagesCount, Stomp.Headers.Subscribe.AckModeValues.CLIENT);
        assertTrue("receivedAfterUnsubscribe was " + receivedAfterUnsubscribe, receivedAfterUnsubscribe <= 1);
    }

    @Test
    public void test_ClientIndividualAck() throws Exception {
        int messagesCount = 1000;
        int receivedAfterUnsubscribe = checkPrefetchSizeReception(messagesCount, Stomp.Headers.Subscribe.AckModeValues.INDIVIDUAL);
        assertTrue("receivedAfterUnsubscribe was " + receivedAfterUnsubscribe, receivedAfterUnsubscribe <= 1);
    }

    @Override
    public Client createConnection() {
        return null;
    }

}
