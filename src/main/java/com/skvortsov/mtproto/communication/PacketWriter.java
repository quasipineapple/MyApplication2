package com.skvortsov.mtproto.communication;

import com.skvortsov.mtproto.Packet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by skvortsov on 9/23/13.
 */
public class PacketWriter {

    private final BlockingQueue<Packet> queue;
    private OutputStream writer;
    private MTPConnection connection;
    private boolean done;
    private Thread writerThread;

    public PacketWriter(MTPConnection connection) {

        this.queue = new ArrayBlockingQueue<Packet>(500, true);
        this.connection = connection;
        init();

    }

    public void init() {

        this.writer = connection.writer;
        done = false;
        writerThread = new Thread(){
            @Override
            public void run() {
                writePackets(this);
            }
        };
    }

    private void writePackets(Thread thread) {
        try{
            while(!done && (writerThread == thread)){
                Packet packet = nextPacket();
                if(packet != null){
                    synchronized (writer){
                        writer.write(packet.array());
                        writer.flush();
                    }
                }
            }

            synchronized (writer){
                while(!queue.isEmpty()){
                    Packet packet = queue.remove();
                    writer.write(packet.array());

                }
                writer.flush();
            }
        }
        catch (IOException ioe){
            if(!done){
                done = true;
                //TODO: connection.packetReader.notifyConnectionError(ioe);
            }

        }
    }

    private Packet nextPacket() {

        Packet packet = null;

        while(!done && (packet = queue.poll()) == null){
            try {
                synchronized (queue){
                    queue.wait();
                }
            }catch (InterruptedException e) {
                //e.printStackTrace();

            }
        }

        return  packet;
    }

    public void startup() {


    }
}
