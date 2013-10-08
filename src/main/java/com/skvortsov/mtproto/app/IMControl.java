package com.skvortsov.mtproto.app;

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
    private final static IMControl imControl = new IMControl();
    private static final String LOG_TAG = "IMControl";

    private IMControl() {

    }

    public static IMControl getInstance(){
        return imControl;
    }

    public void connect() {

        if(server != null && server.getPort() != 0 && server.getAddress() != null){

            try {
                config = new ConnectionConfiguration(server.getAddress(),
                        server.getPort());

                connection = new MTPConnection(config);
                account = new AccountInfo();

                connection.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void setServer(Server server) {
        this.server = server;
    }
}
