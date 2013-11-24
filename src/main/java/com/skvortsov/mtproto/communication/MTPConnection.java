package com.skvortsov.mtproto.communication;

import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.ConstructorCollector;
import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.filter.ConstructorFilter;
import com.skvortsov.mtproto.mtp_api.Auth;

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

    private ConnectionConfiguration configuration;

    private PacketReader packetReader;
    private PacketWriter packetWriter;

    private static AtomicInteger connectionCounter = new AtomicInteger(0);

    int connectionCounterValue = connectionCounter.getAndIncrement();
    public boolean connected = false;
    public boolean authenticated = false;


    public void connect() throws Exception {

        connectUsingConfiguration(configuration);

        //if(connected){

        //    auth_sendCode(getConfiguration().getPhone());
        //}

    }

    public synchronized void auth_sendCode() throws Exception {}



    private void connectUsingConfiguration(ConnectionConfiguration config) throws Exception {

        this.host = config.getHost();
        this.port = config.getPort();

        this.socket = new Socket(host, port);

        initConnection();

    }

    public void sendPacket(Packet packet){
        if (!connected) {
            throw new IllegalStateException("Not connected to server.");
        }
        if (packet == null) {
            throw new NullPointerException("Packet is null.");
        }

        packetWriter.sendPacket(packet);

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

        connected = true;


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
        this.configuration = config;
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

    public ConnectionConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ConnectionConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public ConstructorCollector createConstructorCollector(ConstructorFilter filter) {

        return packetReader.createConstructorCollector(filter);
    }


}
