package com.skvortsov.mtproto.app;

import android.util.Log;

import com.skvortsov.mtproto.AccountInfo;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.Server;
import com.skvortsov.mtproto.communication.ConnectionConfiguration;
import com.skvortsov.mtproto.communication.MTPConnection;
import com.skvortsov.mtproto.mtp_api.Auth;

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


    public String auth_sendCode() {

        String result;

        if(connection == null || !connection.isConnected()){
            result = SERVER_NOT_CONNECTED;
        }else{

            try {
                if(!connection.connected){
                    throw new Exception("Not connected to server.");
                }
                if(connection.authenticated){
                    throw new Exception("Already logged in to server.");
                }

                Constructor response;

                //response = new Auth(connection).sendCode("+79254326805", 0, 1463, "437a55dcaee748fc1596a1bb5e6ca7db");
                response = new Auth(connection).sendCode(account.getPhone_number(),
                        account.getSms_type(), 1463, "437a55dcaee748fc1596a1bb5e6ca7db");

                if(response == null){
                    throw new Exception("No response from server");
                }
                //System.out.println(response.toString());
                Constructor constructor = (Constructor) response.getParamByName("result").getData();;

                if(!constructor.getPredicate().equals("auth.sentCode")){
                    throw new Exception("Not valid response.");
                }

                String phone_registered = ((Constructor) constructor.getParamByName("phone_registered").getData()).getPredicate();

                if(phone_registered.equals("boolTrue")){
                    account.setPhone_registered(true);
                }else if(phone_registered.equals("boolFalse")){
                    account.setPhone_registered(false);
                }

                account.setPhone_code_hash(Helpers.BytesToText((byte[]) constructor.getParamByName("phone_code_hash").getData()));

                result = account.getPhone_registered().toString();
                System.out.println(constructor.toString());

                //authenticated = true;
            } catch (Exception e) {
                result = e.toString();
                e.printStackTrace();
            }
        }

        return result;
    }

    public MTPConnection getConnection() {
        return connection;
    }

    public void setConnection(MTPConnection connection) {
        this.connection = connection;
    }

    public String auth_signIn() {

        String result;

        if(connection == null || !connection.isConnected()){
            result = SERVER_NOT_CONNECTED;
        }else{

            try {
                if(!connection.connected){
                    throw new Exception("Not connected to server.");
                }
                if(connection.authenticated){
                    throw new Exception("Already logged in to server.");
                }

                Constructor response;

                //response = new Auth(connection).sendCode("+79254326805", 0, 1463, "437a55dcaee748fc1596a1bb5e6ca7db");
                response = new Auth(connection).signIn(account.getPhone_number(),
                        account.getPhone_code_hash(), account.getPhone_code());

                if(response == null){
                    throw new Exception("No response from server");
                }
                System.out.println(response.toString());

                Constructor constructor = (Constructor) response.getParamByName("result").getData();
                if(constructor.getPredicate().equals("auth.authorization")){
                    int expires = (Integer) constructor.getParamByName("expires").getData();
                    Constructor user = (Constructor) constructor.getParamByName("user").getData();
                    result = constructor.getPredicate();
                }else if(constructor.getPredicate().equals("rpc_error")){
                    result = Helpers.BytesToText((byte[]) constructor.getParamByName("error_message").getData());
                }else{
                    throw new Exception("Not valid response.");
                }

            } catch (Exception e) {
                result = e.toString();
                e.printStackTrace();
            }
        }

        return result;


    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }
}
