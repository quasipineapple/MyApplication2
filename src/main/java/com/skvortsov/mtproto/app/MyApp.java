package com.skvortsov.mtproto.app;

import android.app.Application;

import com.skvortsov.mtproto.SessionManager;

/**
 * Created by сергей on 22.08.13.
 */
public class MyApp extends Application {

    public static final String LOG_TAG = "MyApp";
    private static MyApp sMyApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sMyApp = this;




    }

    synchronized public static MyApp getApplication() {
        return sMyApp;
    }
}
