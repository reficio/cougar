package org.reficio.cougar.impl;

import org.reficio.cougar.StompConnectionException;
import org.reficio.cougar.StompEncodingException;
import org.reficio.cougar.StompException;
import org.reficio.cougar.StompIOException;
import org.reficio.cougar.core.StompWireFormat;
import org.reficio.cougar.core.TransmissionHandler;
import org.reficio.cougar.domain.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by IntelliJ IDEA.
 * User: dipesh
 * Date: 09/10/11
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
class TransmissionHandlerImpl implements TransmissionHandler {

    private static final transient Logger log = LoggerFactory.getLogger(TransmissionHandler.class);

    protected Socket socket;
    protected Writer writer;
    protected Reader reader;
    protected StompWireFormat wireFormat;

    private String hostname;
    private int port;
    private String encoding;

    TransmissionHandlerImpl(StompWireFormat wireFormat, String hostname, int port, String encoding) {
        this.hostname = hostname;
        this.port = port;
        this.encoding = encoding;
        this.wireFormat = wireFormat;
    }

    // ----------------------------------------------------------------------------------
    // Communication and socket handlers
    // ----------------------------------------------------------------------------------
    public void initializeCommunication(int timeout) {
        initializeSocket(timeout);
        initializeStreams(timeout);
    }

    protected void initializeSocket(int timeout) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostname, port), timeout);
        } catch (IOException e) {
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    protected void initializeStreams(int timeout) {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding));
        } catch (UnsupportedEncodingException e) {
            throw new StompEncodingException("Error during connection initialization", e);
        } catch (IOException e) {
            throw new StompConnectionException("Error during connection initialization", e);
        }
    }

    public void closeCommunication() {
        try {
            closeStreams();
        } finally {
            closeSocket();
        }
    }

    public void setReceptionTimeoutInMillis(int timeout) {
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
            throw new StompIOException("Error error in the underlying IO protocol", ex);
        }
    }

    protected void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

    protected void closeStreams() {
        closeReader();
        closeWriter();
    }

    protected void closeReader() {
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

    protected void closeWriter() {
        try {
            if (writer != null)
                writer.close();
        } catch (IOException e) {
            // Ignore that
        }
    }

    public Frame unmarshall() {
        if (log.isInfoEnabled()) {
            log.info("Receiving frame: ");
        }
        Frame frame = wireFormat.unmarshal(reader);
        if (log.isInfoEnabled()) {
            log.info(frame.toString());
        }
        return frame;
    }

    public void marshall(Frame frame) throws StompException {
        if (log.isInfoEnabled()) {
            log.info("Sending frame: \n" + frame);
        }
        wireFormat.marshal(frame, writer);
    }

}