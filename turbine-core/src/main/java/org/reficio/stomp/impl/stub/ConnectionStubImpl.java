package org.reficio.stomp.impl.stub;

import org.reficio.stomp.StompException;
import org.reficio.stomp.core.FrameDecorator;
import org.reficio.stomp.core.StompOperations;
import org.reficio.stomp.core.StompResource;
import org.reficio.stomp.domain.Command;
import org.reficio.stomp.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

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
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void abort(String transactionId) {
        abort(transactionId, emptyDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        checkNotNull(messageId, "messageId cannot be null");
        Frame frame = new Frame(Command.ACK);
        frame.messageId(messageId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public void ack(String messageId) {
        ack(messageId, emptyDecorator);
    }


    public void begin(String transactionId, FrameDecorator frameDecorator) {
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.BEGIN);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }


    public void begin(String transactionId) {
        begin(transactionId, emptyDecorator);
    }


    public void commit(String transactionId, FrameDecorator frameDecorator) {
        checkNotNull(transactionId, "transactionId cannot be null");
        Frame frame = new Frame(Command.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }


    public void commit(String transactionId) {
        commit(transactionId, emptyDecorator);
    }

    @Override
    public void send(String destination, FrameDecorator frameDecorator) {
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SEND);
        frame.destination(destination);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    @Override
    public String subscribe(String destination, FrameDecorator frameDecorator) {
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SUBSCRIBE);
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
        checkNotNull(id, "id cannot be null");
        checkNotNull(destination, "destination cannot be null");
        Frame frame = new Frame(Command.SUBSCRIBE);
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
        checkNotNull(id, "id cannot be null");
        Frame frame = new Frame(Command.UNSUBSCRIBE);
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
