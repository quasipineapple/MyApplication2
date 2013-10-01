package com.skvortsov.mtproto.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by skvortsov on 9/21/13.
 */
public class MTPConnection {

    OutputStream writer;
    InputStream reader;

    String host;
    int port;
    Socket socket;

    public static boolean DEBUG_ENABLED = false;

    private ConnectionConfig config;

    private PacketReader packetReader;
    private PacketWriter packetWriter;

    private static AtomicInteger connectionCounter = new AtomicInteger(0);

    int connectionCounterValue = connectionCounter.getAndIncrement();
    public boolean connected = false;

    public void connect() throws IOException {

        this.host = config.getHost();
        this.port = config.getPort();
        this.socket = new Socket(host, port);
        initConnection();
    }

    public void sendPacket(){

    }

    private void initConnection(){

        boolean isFirstInitialization = packetReader == null || packetWriter == null;

        if(isFirstInitialization){
            packetWriter = new PacketWriter(this);
            packetReader = new PacketReader(this);

        }else{
            packetReader.init();
            packetWriter.init();
        }

        initReaderAndWriter();
    }

    private void initReaderAndWriter() {
        try {
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MTPConnection(ConnectionConfig config) {
        this.config = config;
    }



    public OutputStream getWriter() {
        return writer;
    }

    public void setWriter(OutputStream writer) {
        this.writer = writer;
    }

    public InputStream getReader() {
        return reader;
    }

    public void setReader(InputStream reader) {
        this.reader = reader;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
