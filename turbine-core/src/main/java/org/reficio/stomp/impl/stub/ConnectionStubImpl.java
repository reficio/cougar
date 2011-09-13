package org.reficio.stomp.impl.stub;

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.CommandType;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 13/09/11
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionStubImpl<T extends StompResource> extends ClientStubImpl<T> implements StompOperations {

    private static final transient Logger log = LoggerFactory.getLogger(ConnectionStubImpl.class);

    protected ConnectionStubImpl() {
        super();
    }

    public void abort(String transactionId, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void abort(String transactionId) {
        abort(transactionId, emptyDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.ACK);
        frame.messageId(messageId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public void ack(String messageId) {
        ack(messageId, emptyDecorator);
    }


    public void begin(String transactionId, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.BEGIN);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }


    public void begin(String transactionId) {
        begin(transactionId, emptyDecorator);
    }


    public void commit(String transactionId, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }


    public void commit(String transactionId) {
        commit(transactionId, emptyDecorator);
    }

    @Override
    public void send(String destination, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.SEND);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public String subscribe(String destination, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.SUBSCRIBE);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        String subscriptionId = frame.subscriptionId();
        if (subscriptionId == null) {
            subscriptionId = UUID.randomUUID().toString();
        }
        frame.subscriptionId(subscriptionId);
        send(frame);
        return subscriptionId;
    }

    @Override
    public String subscribe(String destination) throws StompException {
        return subscribe(destination, emptyDecorator);
    }

    @Override
    public String subscribe(String id, String destination) throws StompException {
        return subscribe(id, destination, emptyDecorator);
    }

    @Override
    public String subscribe(String id, String destination, FrameDecorator frameDecorator) throws StompException {
        Frame frame = new Frame(CommandType.SUBSCRIBE);
        frame.destination(destination);
        frame.subscriptionId(id);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
        return id;
    }

    @Override
    public void unsubscribe(String id) {
        unsubscribe(id, emptyDecorator);
    }

    @Override
    public void unsubscribe(String id, FrameDecorator frameDecorator) {
        Frame frame = new Frame(CommandType.UNSUBSCRIBE);
        frame.subscriptionId(id);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    protected FrameDecorator emptyDecorator = new FrameDecorator() {
        @Override
        public void decorateFrame(Frame frame) {
        }
    };

}
