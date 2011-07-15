package org.reficio.stomp.perf.sender;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 */
public interface ISender {

    void initialize(String hostname, int port, String username, String password, String encoding) throws Exception;
    void close() throws Exception;
    void send(String queue, String payload) throws Exception;
    void send(String queue, String receiptId, String payload) throws Exception;
    String receiveReceipt() throws Exception;

}
