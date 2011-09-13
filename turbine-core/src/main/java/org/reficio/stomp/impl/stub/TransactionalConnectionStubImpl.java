package org.reficio.stomp.impl.stub;

import org.reficio.stomp.StompException;
import org.reficio.stomp.StompIllegalTransactionStateException;
import org.reficio.stomp.StompInvalidHeaderException;
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
public class TransactionalConnectionStubImpl<T extends StompResource> extends ConnectionStubImpl<T> implements StompOperations {

    private static final transient Logger log = LoggerFactory.getLogger(TransactionalConnectionStubImpl.class);

    protected String transactionId;

    protected TransactionalConnectionStubImpl() {
        super();
    }

    // ----------------------------------------------------------------------------------
    // Overridden transaction-aware methods
    // ----------------------------------------------------------------------------------
    // TODO Be aware that ack acknowledges all previous not-acknowledged messages too
    @Override
    public void ack(String messageId) {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator();
        super.ack(messageId, txDecorator);
    }

    @Override
    public void ack(String messageId, FrameDecorator frameDecorator) {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.ack(messageId, txDecorator);
    }

    @Override
    public void send(String destination, final FrameDecorator frameDecorator) throws StompException {
        TransactionAwareDecorator txDecorator = new TransactionAwareDecorator(frameDecorator);
        super.send(destination, txDecorator);
    }

    // ----------------------------------------------------------------------------------
    // StompTransactionalConnection methods - also transaction-aware :)
    // ----------------------------------------------------------------------------------
    public void begin() {
        assertNotInTransaction();
        this.transactionId = UUID.randomUUID().toString();
        log.info(String.format("Beginning transaction id=[%s]", transactionId));
        try {
            begin(transactionId);
        } catch (RuntimeException ex) {
            this.transactionId = null;
            throw ex;
        }
    }

    public void rollback(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(CommandType.ABORT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void rollback() throws StompException {
        abort(transactionId, emptyDecorator);
    }

    public void commit(FrameDecorator frameDecorator) throws StompException {
        assertInTransaction();
        Frame frame = new Frame(CommandType.COMMIT);
        frame.transaction(transactionId);
        preprocessor.decorate(frame, frameDecorator);
        send(frame);
    }

    public void commit() throws StompException {
        assertInTransaction();
        log.info(String.format("Committing transaction id=[%s]", transactionId));
        commit(transactionId);
    }

    // ----------------------------------------------------------------------------------
    // Helper methods - connection state verification
    // ----------------------------------------------------------------------------------
    protected boolean isInTransaction() {
        return this.transactionId != null;
    }

    protected void assertInTransaction() {
        if (isInTransaction() == false) {
            throw new StompIllegalTransactionStateException("Transaction has not begun");
        }
    }

    protected void assertNotInTransaction() {
        if (isInTransaction() == true) {
            throw new StompIllegalTransactionStateException("Transaction has begun");
        }
    }

    class TransactionAwareDecorator implements FrameDecorator {
        public TransactionAwareDecorator() {
            this.originalDecorator = null;
        }

        public TransactionAwareDecorator(final FrameDecorator originalDecorator) {
            this.originalDecorator = originalDecorator;
        }

        private FrameDecorator originalDecorator;

        @Override
        public void decorateFrame(Frame frame) {
            if (originalDecorator != null) {
                originalDecorator.decorateFrame(frame);
            }
            if (frame.transaction() != null) {
                throw new StompInvalidHeaderException("TransactionId header can't be set manually in transactional connection");
            }
            frame.transaction(transactionId);
        }
    }

}
