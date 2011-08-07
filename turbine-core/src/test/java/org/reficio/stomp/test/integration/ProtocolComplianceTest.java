package org.reficio.stomp.test.integration;

import org.apache.activemq.broker.BrokerService;
import org.junit.*;
import org.reficio.stomp.connection.Connection;
import org.reficio.stomp.connection.TransactionalConnection;
import org.reficio.stomp.core.StompTransactionalConnection;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.domain.AckType;
import org.reficio.stomp.domain.Frame;
import org.reficio.stomp.impl.ConnectionImpl;
import org.reficio.stomp.impl.StompTxConnectionImpl;
import org.reficio.stomp.test.unit.TxConnectionTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2011-07-11
 * Time: 7:59 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class ProtocolComplianceTest {

    private static BrokerService broker;

    private String stompQueuePrefix = "/queue/";
    private String destinationName = "request";

    @BeforeClass
    public static void initialize() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("stomp://localhost:61613");
        broker.start();
    }

    @AfterClass
    public static void stop() throws Exception {
        broker.stop();
    }

    @Before
    public void setup() throws Exception {
        broker.getAdminView().addQueue(destinationName);
    }

    @After
    public void cleanup() throws Exception {
        broker.getAdminView().removeQueue(destinationName);
    }

    private Connection createConnection() {
        Connection conn = ConnectionImpl.create().hostname("localhost").port(61613);
        conn.init();
        return conn;
    }

    private TransactionalConnection createTransactionalCnnection() {
        TransactionalConnection conn = StompTxConnectionImpl.create().hostname("localhost").port(61613);
        conn.init();
        return conn;
    }


    @Test
    public void checkSingleReceptionWithSubscribeUnsubscribeNoTx() throws Exception {

        final String receiptId = UUID.randomUUID().toString();
        Connection connSender = createConnection();
        String[] payloads = new String[]{"Jason Bourne", "James Bond"};
        for (final String payload : payloads) {
            connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(payload);
                    frame.receipt(receiptId);
                }
            });
            Frame receipt = connSender.receive();
            assertEquals(receiptId, receipt.receiptId());
        }
        connSender.close();

        Connection connReceiver = createConnection();
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName, new FrameDecorator() {
            @Override
            public void decorateFrame(Frame frame) {
                frame.ack(AckType.CLIENT);
            }
        });
        // receive and ack first frame
        Frame frame1 = connReceiver.receive();
        connReceiver.ack(frame1.messageId());
        // receive second frame and DO NOT ack
        Frame frame2 = connReceiver.receive();
        // unsubscribe and commit
        connReceiver.unsubscribe(subsId);


        // second message should be redelivered
        Connection connReceiver2 = createConnection();
        final String subsId2 = connReceiver2.subscribe(stompQueuePrefix + destinationName);
        Frame frame3 = connReceiver2.receive();
        assertEquals(frame2.payload(), frame3.payload());
        connReceiver2.unsubscribe(subsId2);
        connReceiver2.close();
    }

    @Test
    public void checkSingleReceptionWithSubscribeUnsubscribeTx() throws Exception {

        final String receiptId = UUID.randomUUID().toString();
        StompTransactionalConnection connSender = factory.createConnection();
        connSender.setAutoAcknowledge(false);
        String[] payloads = new String[]{"Jason Bourne", "James Bond"};
        for (final String payload : payloads) {
            connSender.send(stompQueuePrefix + destinationName, new FrameDecorator() {
                @Override
                public void decorateFrame(Frame frame) {
                    frame.payload(payload);
                    frame.receipt(receiptId);
                }
            });
            Frame receipt = connSender.receive();
            assertEquals(receiptId, receipt.receiptId());
        }
        connSender.commit();
        connSender.close();

        StompTransactionalConnection connReceiver = factory.createConnection();
        connReceiver.setAutoAcknowledge(false);
        final String subsId = connReceiver.subscribe(stompQueuePrefix + destinationName);
        // receive and ack first frame
        Frame frame1 = connReceiver.receive();
        connReceiver.ack(frame1.messageId());
        // receive second frame and DO NOT ack
        Frame frame2 = connReceiver.receive();
        // unsubscribe and commit
        connReceiver.unsubscribe(subsId);
        connReceiver.commit();

        // second message should be redelivered
        StompTransactionalConnection connReceiver2 = factory.createConnection();
        connReceiver2.setAutoAcknowledge(false);
        final String subsId2 = connReceiver2.subscribe(stompQueuePrefix + destinationName);
        Frame frame3 = connReceiver2.receive();
        assertEquals(frame2.payload(), frame3.payload());
        connReceiver2.unsubscribe(subsId2);
        connReceiver2.commit();
        connReceiver2.close();
    }

}
