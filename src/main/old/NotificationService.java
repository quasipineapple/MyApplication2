package com.skvortsov.mtproto.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.skvortsov.mtproto.Packet;
import com.skvortsov.mtproto.WatchData;
import com.skvortsov.mtproto.WatchSocket;
import old.INotificationServiceManager;

import java.io.IOException;

/**
 * Created by skvortsov on 09.08.13.
 */
public class NotificationService extends Service implements INotificationServiceManager {

    static final String TAG = "NotificationService";
    public static final String TAKE_MESSAGE = "Take_Message";
    private IBinder mBinder = new LocalBinder();
    WatchData data;

    private NotificationManager mNM;
    private ConnectivityManager conManager;

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String sendMessage(String username, String tousername, String message) {
        return null;
    }

    @Override
    public void sendPacket(Packet p) {

        try {
            data.dos.write(p.array());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String authenticateUser(String usernameText, String passwordText) {
        return null;
    }

    @Override
    public void messageReceived(String username, String message) {

    }

    @Override
    public boolean isNetworkConnected() {
        return conManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public boolean isUserAuthenticated() {
        return false;
    }

    @Override
    public String getLastRawFriendList() {
        return null;
    }

    @Override
    public void exit() {

    }

    @Override
    public String signUpUser(String usernameText, String passwordText, String email) {
        return null;
    }

    @Override
    public String addNewFriendRequest(String friendUsername) {
        return null;
    }

    @Override
    public String sendFriendsReqsResponse(String approvedFriendNames, String discardedFriendNames) {
        return null;
    }

    public class LocalBinder extends Binder {
        public INotificationServiceManager getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        //   showNotification();
        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        startService();

    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "ImService started");

    }

    @Override
    public void onDestroy() { }

    // Здесь выполняем инициализацию нужных нам значений
    // и открываем наше сокет-соединение
    private void startService() {

        //acc_email = "test@test.com";

        try {
            openConnection();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // данный метод открыает соединение
    public void openConnection() throws InterruptedException
    {
        try {

            // WatchData - это класс, с помощью которого мы передадим параметры в
            // создаваемый поток
            data = new WatchData();
            //data.email = acc_email;
            data.ctx = this;

            // создаем новый поток для сокет-соединения
            new WatchSocket().execute(data);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
