package com.skvortsov.mtproto.communication;

import com.skvortsov.mtproto.EncryptedMessageManager;
import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.PacketManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by skvortsov on 9/23/13.
 */
public class PacketReader {

    private Thread readerThread;
    private MTPConnection connection;
    private InputStream reader;
    private ExecutorService listenerExecutor;
    private boolean done;
    private Semaphore connectionSemaphore;
    private boolean connected;

    public PacketReader(MTPConnection connection) {
        this.connection = connection;
        init();
    }

    public void init(){

        connected = false;
        done = false;
        readerThread = new Thread(){
            @Override
            public void run() {
                parsePackets(this);
            }
        };

        readerThread.setName("MTProto Packet Reader (" + connection.connectionCounterValue + ")");
        readerThread.setDaemon(true);

    }


    public void startup() throws Exception {



        connectionSemaphore = new Semaphore(1);

        readerThread.start();

        try {
            connectionSemaphore.acquire();
            int waitTime = AppConfig.getPacketReplyTimeout();
            connectionSemaphore.tryAcquire(waitTime, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        if(!connected){
            throw new Exception("Connection failed. No response from server.");
        }else{
            connection.connected = true;
        }
    }


    private void parsePackets(Thread thread){

        int read;
        byte[] data;
        byte[] buffer = new byte[4096];
        try {
            while ((read = reader.read(buffer)) >= 0 && thread == readerThread && !done){
                data = new byte[read];
                System.arraycopy(buffer, 0, data, 0, read);
                if(read > 0){
                    processPacket(PacketManager.parse(data));
                }
            }

        } catch (IOException e) {
            //e.printStackTrace();
            if(!done){
                notifyConnectionError(e);
            }
        }


    }

    private void notifyConnectionError(IOException e) {

        done = true;
        e.printStackTrace();
        connection.shutdown();

        //TODO: Notify connection listeners of the error.





    }

    private void processPacket(Packet packet) {



    }
}
