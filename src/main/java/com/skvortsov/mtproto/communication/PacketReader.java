package com.skvortsov.mtproto.communication;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.ConnectionListener;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.ConstructorCollector;
import com.skvortsov.mtproto.ConstructorListener;
import com.skvortsov.mtproto.ConstructorPredicateFilter;
import com.skvortsov.mtproto.Data;
import com.skvortsov.mtproto.DataListener;
import com.skvortsov.mtproto.Msg;
import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.PacketManager;
import com.skvortsov.mtproto.interfaces.ConstructorFilter;

import java.io.IOException;
import java.io.InputStream;
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
    private InputStream reader;
    private ExecutorService listenerExecutor;
    private boolean done;
    private Semaphore connectionSemaphore;
    private Collection<ConstructorCollector> collectors = new ConcurrentLinkedQueue<ConstructorCollector>();

    protected final Map<ConstructorListener, ConstructorListenerWrapper> listeners =
            new ConcurrentHashMap<ConstructorListener, ConstructorListenerWrapper>();
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

        reader = connection.getReader();

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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }


    }

    private void notifyConnectionError(IOException e) {

        done = true;
        e.printStackTrace();
        connection.shutdown();

        //TODO: Notify connection listeners of the error.





    }


    private void addListener() {


        ConstructorFilter constructorFilter = new ConstructorPredicateFilter("msg_container");

        ConstructorListener constructorListener = new ConstructorListener() {
            @Override
            public void processConstructor(Constructor constructor) {
                List<Msg> msg_need_ack = new ArrayList<Msg>();

                Long msgc_id = ByteBuffer.wrap(d.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();

                for(Msg m: d.toMsgArray()){

                    Long m_id = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
                    Constructor c = m.toConstructor();

                    //processConstructor(c);

                    //System.out.println("Msg (" + m_id + ") " + c.toString());
                    if(need_ack(m)){
                        msg_need_ack.add(m);
                    }
                }

                send_msgs_ack(msg_need_ack);
            }
        }



    }

    private void processPacket(Packet packet) throws NoSuchAlgorithmException, CloneNotSupportedException, IOException {

        if(packet == null){
          return;
        }

        System.out.println(packet.getSize());
        System.out.println(packet.toString());

        Data data = packet.toEncryptedMessage().toData();
        Constructor constructor = data.toConstructor();

        for(ConstructorCollector collector : collectors){
            collector.processConstructor(constructor);
        }

        listenerExecutor.submit(new ConstructorListenerNotification(constructor));

        /*
        if(d.isMsgContainer()){


        }else{
            Constructor c = d.toConstructor();

            processConstructor(c);
            if(c.getPredicate().equals("bad_server_salt")){
                SessionManager.getS().setSalt(
                        Helpers.LongTobyteArray(
                                (Long) c.getParamByName("new_server_salt").getData()
                        )
                );

            }
        }*/

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

        connection.sendPacket(dd.toEncryptedMessage().toPacket());
        /*socketOperator.sendHttpRequest(
                dd.toEncryptedMessage().array());*/
        //Data dd2 = EncryptedMessageManager.parse2(answer).toData();


        //getAnswer(dd2);

    }

    private void processConstructor(Constructor constructor){

    }

    private boolean need_ack(Msg m) throws CloneNotSupportedException {

        //if(m.toConstructor().getPredicate().equals("bad_msg_notification")){

        //    return false;
        //}
        return true;

    }

    public void shutdown() {
        // Notify connection listeners of the connection closing if done hasn't already been set.
        if (!done) {
            for (ConnectionListener listener : connectionListeners) {
                try {
                    listener.connectionClosed();
                }
                catch (Exception e) {
                    // Cath and print any exception so we can recover
                    // from a faulty listener and finish the shutdown process
                    e.printStackTrace();
                }
            }
        }
        done = true;

        // Shut down the listener executor.
        listenerExecutor.shutdown();
    }

    public ConstructorCollector createConstructorCollector(ConstructorFilter filter) {

        ConstructorCollector collector = new ConstructorCollector(this, filter);
        collectors.add(collector);
        // Add the collector to the list of active collector.
        return collector;
    }


    private class ConstructorListenerNotification implements Runnable {

        private Constructor constructor;

        public ConstructorListenerNotification(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public void run() {
            for(ConstructorListenerWrapper listenerWrapper : listeners.values()){
                listenerWrapper.notifyListener(constructor);
            }
        }
    }

    private class ConstructorListenerWrapper {

        private ConstructorListener constructorListener;
        private ConstructorFilter constructorFilter;

        public ConstructorListenerWrapper(ConstructorListener constructorListener, ConstructorFilter constructorFilter) {
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
