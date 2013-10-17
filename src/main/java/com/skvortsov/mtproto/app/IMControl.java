package com.skvortsov.mtproto.app;

import android.util.Log;

import com.skvortsov.mtproto.AccountInfo;
import com.skvortsov.mtproto.Server;
import com.skvortsov.mtproto.communication.ConnectionConfiguration;
import com.skvortsov.mtproto.communication.MTPConnection;

/**
 * Created by skvortsov on 10/8/13.
 */
public class IMControl {

    private Server server;
    private ConnectionConfiguration config;
    private MTPConnection connection;
    private AccountInfo account;

    public final static String LOGIN_SUCCESS = "Login successfully";
    public static final String SERVER_NOT_CONNECTED = "Server not connected";
    private final static IMControl imControl = new IMControl();
    private static final String LOG_TAG = "IMControl";

    private IMControl() {}

    public static IMControl getInstance(){
        return imControl;
    }

    public void connect() {
        if(server != null && server.getPort() != 0 && server.getAddress() != null){
            try {
                config = new ConnectionConfiguration(server.getAddress(), server.getPort());
                connection = new MTPConnection(config);
                account = new AccountInfo();
                connection.connect();
                Log.v(LOG_TAG, "Connected to the server " + server.getAddress()
                        + " " + Integer.toString(server.getPort()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }


    public String login() {

        String result = LOGIN_SUCCESS;

        if(connection == null || !connection.isConnected()){
            result = SERVER_NOT_CONNECTED;
        }else{

            try {
                connection.login();
            } catch (Exception e) {
                result = e.toString();
                e.printStackTrace();
            }
        }

        return result;
    }
}
