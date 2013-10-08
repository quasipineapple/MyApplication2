package com.skvortsov.mtproto.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private ConnectionConfiguration config;

    private PacketReader packetReader;
    private PacketWriter packetWriter;

    private static AtomicInteger connectionCounter = new AtomicInteger(0);

    int connectionCounterValue = connectionCounter.getAndIncrement();
    public boolean connected = false;

    public void connect() throws Exception {

        connectUsingConfiguration(config);

        if(connected){

        }


    }

    private void connectUsingConfiguration(ConnectionConfiguration config) throws Exception {

        this.host = config.getHost();
        this.port = config.getPort();

        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }


        initConnection();

    }

    public void sendPacket(){

    }

    private void initConnection() throws Exception {

        boolean isFirstInitialization = packetReader == null || packetWriter == null;

        initReaderAndWriter();

        if(isFirstInitialization){
            packetWriter = new PacketWriter(this);
            packetReader = new PacketReader(this);

        }else{
            packetReader.init();
            packetWriter.init();
        }

        packetWriter.startup();

        packetReader.startup();


    }

    private void initReaderAndWriter() {

        try {
            reader = socket.getInputStream();
            writer = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MTPConnection(ConnectionConfiguration config) {
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

    protected void shutdown() {

        connected = false;
        packetReader.shutdown();
        packetWriter.shutdown();

        try {
            Thread.sleep(150);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if(reader != null){
            try {
                reader.close();
            } catch (Throwable ignore){}
            reader = null;
        }

        if(writer != null){
            try {
                writer.close();
            } catch (Throwable ignore){}
            writer = null;
        }

        try {
            socket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }
}
