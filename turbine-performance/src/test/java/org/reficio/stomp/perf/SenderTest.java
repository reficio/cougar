/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.reficio.stomp.perf;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;
import org.reficio.stomp.perf.sender.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public class SenderTest {

    private static final transient Logger log = LoggerFactory.getLogger(SenderTest.class);

    private static BrokerService broker;
    private String stompQueuePrefix = "/queue/";
    private String destinationName = "request";


    private static String hostname = "127.0.0.1";
    private static int port = 61613;
    private static String username = "test";
    private static String password = "test";
    private static String encoding = "UTF-8";


//    @BeforeClass
//    public static void initialize() throws Exception {
//        broker = new BrokerService();
//        broker.setPersistent(false);
//        broker.addConnector(String.format("stomp://%s:%d", hostname, port));
//        broker.start();
//        broker.getAdminView().disableStatistics();
//    }
//
//    @AfterClass
//    public static void stop() throws Exception {
//        broker.stop();
//    }

//    @Before
//    public void setup() throws Exception {
//        broker.getAdminView().addQueue(destinationName);
//    }
//
//    @After
//    public void cleanup() throws Exception {
//        broker.getAdminView().removeQueue(destinationName);
//    }

    public void send(ISender sender) throws Exception {
        sender.initialize(hostname, port, username, password, encoding);
        String payload = "James Bond 007!";
        String receiptIdRequest = UUID.randomUUID().toString();

        sender.send(stompQueuePrefix+destinationName, receiptIdRequest, payload);
        String receiptIdResponse = sender.receiveReceipt();

        assertEquals(receiptIdRequest, receiptIdResponse);
        assertEquals(1, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);


        sender.close();
    }

    public long performanceSendAndReceive(int count, int byteSize, ISender sender) throws Exception {
        sender.initialize(hostname, port, username, password, encoding);

        String payload = RandomStringUtils.randomAlphanumeric(byteSize); //"James Bond 007!";
        String receiptIdRequest = UUID.randomUUID().toString();

        long begin = System.nanoTime();
        for (int i = 0 ; i < count ; i++) {
            sender.send(stompQueuePrefix+destinationName, receiptIdRequest, payload);
            String receiptIdResponse = sender.receiveReceipt();
            assertEquals(receiptIdRequest, receiptIdResponse);
        }
        long end = System.nanoTime();
        // assertEquals(count, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
        return (end - begin) / 1000000;
    }

    public long performanceSend(int count, int byteSize, ISender sender) throws Exception {
        sender.initialize(hostname, port, username, password, encoding);

        String payload = RandomStringUtils.randomAlphanumeric(byteSize); //"James Bond 007!";
        String receiptIdRequest = UUID.randomUUID().toString();

        long begin = System.nanoTime();
        for (int i = 0 ; i < count ; i++) {
            sender.send(stompQueuePrefix+destinationName, payload);
        }
        long end = System.nanoTime();
        // assertEquals(count, broker.getDestination(new ActiveMQQueue(destinationName)).browse().length);
        return (end - begin) / 1000000;
    }

    int count = 20000;
    int size = 128;

    @Test
    public void activemqSendAndReceive() throws Exception {
        long result = performanceSendAndReceive(count, size, new ActiveMQSender());
        log.info("activemqSendAndReceive result = " + result);
    }

    @Test
    public void turbineSendAndReceive() throws Exception {
        long result = performanceSendAndReceive(count, size, new TurbineSender());
        log.info("turbineSendAndReceive result = " + result);
    }

    @Test
    public void gozirraSendAndReceive() throws Exception {
        long result = performanceSendAndReceive(count, size, new GozirraSender());
        log.info("gozirraSendAndReceive result = " + result);
    }


    @Test
    public void activemqSend() throws Exception {
        long result = performanceSend(count, size, new ActiveMQSender());
        log.info("activemqSend result = " + result);
    }

    @Test
    public void turbineSend() throws Exception {
        long result = performanceSend(count, size, new TurbineSender());
        log.info("turbineSend result = " + result);
    }

    @Test
    public void gozirraSend() throws Exception {
        long result = performanceSend(count, size, new GozirraSender());
        log.info("gozirraSend result = " + result);
    }

    @Test
    public void stompjSend() throws Exception {
        long result = performanceSend(count, size, new StompJSender());
        log.info("stompjSend result = " + result);
    }


}
