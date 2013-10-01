package com.skvortsov.mtproto;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by skvortsov on 09.08.13.
 */
public class WatchSocket extends AsyncTask<WatchData , Integer, Integer> {

    private static final String TAG = "WatchSocket";
    Context mCtx;
    Socket mySock;

    protected void onProgressUpdate(Integer... progress){

    }

    protected void onPostExecute(Integer result){
        // Это выполнится после завершения работы потока
    }

    protected Integer doInBackground(WatchData... param){

        InetAddress serverAddr;

        mCtx = param[0].ctx;
        //String email = param[0].email;

        try {
            while(true)
            {
                serverAddr = InetAddress.getByName("95.142.192.65");
                mySock = new Socket(serverAddr, 80);

                // открываем сокет-соединение
                SocketData data = new SocketData();
                data.ctx = mCtx;
                data.sock = mySock;

                GetPacket pack = new GetPacket();
                AsyncTask<SocketData, Integer, Integer> running = pack.execute(data);


                //String message = email;
                // Посылаем email на сервер
                try {
                    //PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(mySock.getOutputStream())),true);
                    param[0].dos = new DataOutputStream(mySock.getOutputStream());
                    //out.println(message);
                    Log.i(TAG, "WatchSocket запущен.");

                } catch(Exception e){
                    e.printStackTrace();
                }

                // Следим за потоком, принимающим сообщения
                while(running.getStatus().equals(Status.RUNNING)){

                }

                Log.e(TAG, "Соединение разорвано.");
                // Если поток закончил принимать сообщения - это означает,
                // что соединение разорвано (других причин нет).
                // Это означает, что нужно закрыть сокет
                // и открыть его опять в бесконечном цикле (см. while(true) выше)
                try{
                    mySock.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
