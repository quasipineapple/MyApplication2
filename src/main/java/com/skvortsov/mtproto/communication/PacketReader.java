package com.skvortsov.mtproto.communication;


import com.google.common.io.LittleEndianDataInputStream;
import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.ConnectionListener;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.ConstructorCollector;
import com.skvortsov.mtproto.ConstructorListener;
import com.skvortsov.mtproto.Data;
import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.Msg;
import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.PacketManager;
import com.skvortsov.mtproto.SessionManager;
import com.skvortsov.mtproto.filter.ConstructorFilter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by skvortsov on 9/23/13.
 */
public class PacketReader {

    private Thread readerThread;
    private MTPConnection connection;
    private LittleEndianDataInputStream reader;
    private ExecutorService listenerExecutor;
    private boolean done;
    private Semaphore connectionSemaphore;
    private Collection<ConstructorCollector> collectors = new ConcurrentLinkedQueue<ConstructorCollector>();

    protected final Map<ConstructorListener, ListenerWrapper> listeners =
            new ConcurrentHashMap<ConstructorListener, ListenerWrapper>();
    protected final Collection<ConnectionListener> connectionListeners =
            new CopyOnWriteArrayList<ConnectionListener>();

    //private boolean connected;

    public PacketReader(MTPConnection connection) {
        this.connection = connection;
        init();
    }

    public void init(){

        //connected = false;
        done = false;
        readerThread = new Thread(){
            @Override
            public void run() {
                parsePackets(this);
            }
        };

        readerThread.setName("MTProto Packet Reader (" + connection.connectionCounterValue + ")");
        readerThread.setDaemon(true);

        reader = new LittleEndianDataInputStream(new BufferedInputStream(connection.getReader()));

        listenerExecutor = Executors.newSingleThreadExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r,
                                "MTProto Listener Processor (" + connection.connectionCounterValue + ")"
                        );
                        thread.setDaemon(true);
                        return  thread;
                    }
                }
        );


    }


    public void startup() throws Exception {

        connectionSemaphore = new Semaphore(1);

        readerThread.start();

        try {
            connectionSemaphore.acquire();
            int waitTime = MTPConfiguration.getPacketReplyTimeout();
            connectionSemaphore.tryAcquire(waitTime, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //if(!connected){
        //    throw new Exception("Connection failed. No response from server.");
        //}else{
            //connection.connected = true;
        //}
    }

    private void parsePackets(Thread thread){

        try {
            while (thread == readerThread && !done){

                for(int segSize; (segSize = reader.readInt()) > 0;) {
                    byte[] bytes = new byte[segSize - 4];
                    reader.readFully(bytes);
                    processPacket(PacketManager.parse2(segSize, bytes));
                }
            }

        } catch (Exception e) {
            //e.printStackTrace();
            if(!done){
                notifyConnectionError(e);
            }
        }


    }

    private void notifyConnectionError(Exception e) {

        done = true;
        e.printStackTrace();
        connection.shutdown();

        //TODO: Notify connection listeners of the error.

    }




    private void processPacket(Packet packet) throws NoSuchAlgorithmException, CloneNotSupportedException, IOException {

        if(packet == null){
          return;
        }

        System.out.println("Read(" + packet.getSize() + ") " + packet.toString());

        Data data  = packet.toEncryptedMessage().toData();

        if(data.isMsgContainer()){
            List<Msg> msg_need_ack = new ArrayList<Msg>();

            //Long msgc_id = ByteBuffer.wrap(data.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();

            for(Msg m: data.toMsgArray()){

                //Long m_id = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
                //Constructor c = m.toConstructor();

                processConstructor(m.toConstructor());

                //System.out.println("Msg (" + m_id + ") " + c.toString());
                if(need_ack(m)){
                    msg_need_ack.add(m);
                }
            }

            send_msgs_ack(msg_need_ack);

        }else{
            Constructor c = data.toConstructor();

            processConstructor(c);
            System.out.println(c.toString());
            if(c.getPredicate().equals("bad_server_salt")){
                SessionManager.getS().setSalt(
                        Helpers.LongTobyteArray(
                                (Long) c.getParamByName("new_server_salt").getData()
                        )
                );

            }
        }

    }


    private void send_msgs_ack(List<Msg> mm) throws CloneNotSupportedException, NoSuchAlgorithmException, IOException {

        Constructor msgs_ack = BookManager.getBook().getConstructorByPredicate("msgs_ack").clone();
        Long[] msgs_ids = new Long[mm.size()];
        int i = 0;
        //StringBuilder sb = new StringBuilder();

        for(Msg m : mm){
            msgs_ids[i++] = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
            //sb.append(ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong());
            //sb.append(";");
        }

        msgs_ack.getParamByName("msg_ids").setData(msgs_ids);
        Data dd = msgs_ack.toData();

        System.out.println("send_msgs_ack(" +
                ByteBuffer.wrap(dd.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong() + ") " +
                "for msg_ids " + Arrays.toString(msgs_ids));

        //ConstructorCollector collector = connection.createConstructorCollector(null);
        connection.sendPacket(dd.toEncryptedMessage().toPacket());

        //System.out.println(collector.nextResult(MTPConfiguration.getPacketReplyTimeout()));
        /*socketOperator.sendHttpRequest(
                dd.toEncryptedMessage().array());*/
        //Data dd2 = EncryptedMessageManager.parse2(answer).toData();


        //getAnswer(dd2);

    }

    private void processConstructor(Constructor constructor){

        for(ConstructorCollector collector : collectors){
            collector.processConstructor(constructor);
        }

        listenerExecutor.submit(new ListenerNotification(constructor));
    }

    private boolean need_ack(Msg m) throws CloneNotSupportedException {

        //if(m.toConstructor().getPredicate().equals("bad_msg_notification")){

        //    return false;
        //}
        return true;

    }

    public void shutdown() {

    }

    public ConstructorCollector createConstructorCollector(ConstructorFilter filter) {

        ConstructorCollector collector = new ConstructorCollector(this, filter);
        collectors.add(collector);
        // Add the collector to the list of active collector.
        return collector;
    }

    public void cancelPacketCollector(ConstructorCollector constructorCollector) {
        collectors.remove(constructorCollector);
    }


    private class ListenerNotification implements Runnable {

        private Constructor constructor;

        public ListenerNotification(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public void run() {
            for(ListenerWrapper listenerWrapper : listeners.values()){
                listenerWrapper.notifyListener(constructor);
            }
        }
    }

    private class ListenerWrapper {
        private ConstructorListener constructorListener;
        private ConstructorFilter constructorFilter;

        public ListenerWrapper(ConstructorListener constructorListener, ConstructorFilter constructorFilter) {
            this.constructorListener = constructorListener;
            this.constructorFilter = constructorFilter;
        }

        public void notifyListener(Constructor constructor){
            if(constructorFilter == null || constructorFilter.accept(constructor)){
                constructorListener.processConstructor(constructor);
            }
        }
    }
}
