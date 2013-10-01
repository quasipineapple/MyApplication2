package com.skvortsov.mtproto;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.skvortsov.mtproto.services.NotificationService;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by skvortsov on 09.08.13.
 */
public class GetPacket extends AsyncTask<SocketData, Integer, Integer> {

    private static final String TAG = "GetPacket" ;
    Context mCtx;
    byte[] mData;
    Socket mySock;

    protected void onProgressUpdate(Integer... progress)
    {
        try
        {
            // Получаем принятое от сервера сообщение
            //String prop = String.valueOf(mData);
            // Делаем с сообщением, что хотим. Я, например, пишу в базу

            Intent i = new Intent(NotificationService.TAKE_MESSAGE);
            i.putExtra(Packet.PACKET, mData);
            mCtx.sendBroadcast(i);
            Log.w(TAG, "mCtx.sendBroadcast(i);");
            //mCtx.sendBroadcast();

        }
        catch(Exception e)
        {
            Toast.makeText(mCtx, "Socket error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void onPostExecute(Integer result)
    {
        // Это выполнится после завершения работы потока
    }

    protected Integer doInBackground(SocketData... param)
    {
        mySock = param[0].sock;
        mCtx = param[0].ctx;
        //mData = new byte[4096];

        try {

            DataInputStream reader = new DataInputStream(mySock.getInputStream());
            int read;
            //byte[] tempdata;
            byte[] buffer = new byte[4096];
            while ((read = reader.read(buffer)) >= 0 && !isCancelled())
            {
                // "Вызываем" onProgressUpdate каждый раз, когда принято сообщение
                mData = new byte[read];
                System.arraycopy(buffer, 0, mData, 0, read);
                if(read > 0) publishProgress(read);
                //Log.w(TAG, "Got " + read + " bytes: " + Helpers.byteArrayToHex(mData));

            }
            reader.close();
            Log.w(TAG, "reader.close();");

            //int read = nis.read(buffer);
            //if (read != -1) {
            //    tempdata = new byte[read];
            //    System.arraycopy(buffer, 0, tempdata, 0, read);
            //    Log.i("AsyncTask", "doInBackground: Got some data " + Helpers.byteArrayToHex(tempdata));
            //}
            //return tempdata;

            //BufferedReader reader = new BufferedReader(new InputStreamReader(mySock.getInputStream()));


            // Принимаем сообщение от сервера
            // Данный цикл будет работать, пока соединение не оборвется
            // или внешний поток не скажет данному cancel()

        } catch (IOException e) {
            return -1;
        }
        catch (Exception e) {
            return -1;
        }
        return 0;
    }

}
