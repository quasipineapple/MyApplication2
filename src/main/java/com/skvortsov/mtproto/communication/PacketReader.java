package com.skvortsov.mtproto.communication;

import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;

import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.ConstructorCollector;
import com.skvortsov.mtproto.Data;
import com.skvortsov.mtproto.EncryptedMessageManager;
import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.Msg;
import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.PacketManager;
import com.skvortsov.mtproto.SessionManager;
import com.skvortsov.mtproto.interfaces.ConstructorFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private Collection<ConstructorCollector> collectors = new ConcurrentLinkedQueue<ConstructorCollector>();
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

    }


    public void startup() throws Exception {

        connectionSemaphore = new Semaphore(1);

        readerThread.start();

        try {
            connectionSemaphore.acquire();
            int waitTime = AppConfig.getPacketReplyTimeout();
            connectionSemaphore.tryAcquire(waitTime, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //if(!connected){
        //    throw new Exception("Connection failed. No response from server.");
        //}else{
            connection.connected = true;
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

    private void processPacket(Packet packet) throws NoSuchAlgorithmException, CloneNotSupportedException {

        if(packet == null){
          return;
        }

        Data d = packet.toEncryptedMessage().toData();

        if(d.isMsgContainer()){
            List<Msg> msg_need_ack = new ArrayList<Msg>();

            Long msgc_id = ByteBuffer.wrap(d.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
            System.out.println("MsgContainer (" + msgc_id + ") (" + msg_need_ack.size() +") begin; ");
            for(Msg m: d.toMsgArray()){

                Long m_id = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
                Constructor c = m.toConstructor();

                for(ConstructorCollector collector : collectors){
                    collector.processConstructor(c);
                }

                System.out.println("Msg (" + m_id + ") " + c.toString());
                if(need_ack(m)){
                    msg_need_ack.add(m);
                }
            }

            System.out.println("MsgContainer (" + msgc_id + ") end;");
            System.out.println("need ack for " + msg_need_ack.size() + " messages;");


            send_msgs_ack(msg_need_ack);


        }else{
            Constructor c = d.toConstructor();
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

    private boolean need_ack(Msg m) throws CloneNotSupportedException {

        //if(m.toConstructor().getPredicate().equals("bad_msg_notification")){

        //    return false;
        //}
        return true;

    }

    public void shutdown() {

    }

    public ConstructorCollector createConstructorCollector(ConstructorFilter filter) {

        return null;
    }
}
