package com.skvortsov.mtproto.mtp_api;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.Data;
import com.skvortsov.mtproto.Method;
import com.skvortsov.mtproto.communication.MTPConnection;

import java.security.NoSuchAlgorithmException;

/**
 * Created by skvortsov on 10/8/13.
 */
public class Auth {


    private MTPConnection connection;

    public Auth(MTPConnection connection) {
        this.connection = connection;
    }

    public String SendCode(String phone, int sms_type, int api_id, String api_hash) throws Exception {

        Method auth_sendCode = BookManager.getBook().getMethodByName("Auth.sendCode").clone();
        auth_sendCode.getParamByName("phone_number").setData(phone);
        auth_sendCode.getParamByName("sms_type").setData(sms_type);
        auth_sendCode.getParamByName("api_id").setData(api_id);
        auth_sendCode.getParamByName("api_hash").setData(api_hash);

        Data d = auth_sendCode.toData();

        DataCollector collector =
                connection.createDataCollector(new DataFilter(d.getMessage_id()));

        connection.sendPacket(d.toEncryptedMessage().toPacket());



        return "";

    }
}