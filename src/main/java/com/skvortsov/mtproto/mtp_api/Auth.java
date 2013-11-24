package com.skvortsov.mtproto.mtp_api;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.ConstructorCollector;
import com.skvortsov.mtproto.Method;
import com.skvortsov.mtproto.communication.MTPConfiguration;
import com.skvortsov.mtproto.communication.MTPConnection;
import com.skvortsov.mtproto.filter.ConstructorFilter;
import com.skvortsov.mtproto.filter.ConstructorPredicateFilter;
import com.skvortsov.mtproto.filter.OrFilter;

/**
 * Created by skvortsov on 10/8/13.
 */
public class Auth {


    private MTPConnection connection;

    public Auth(MTPConnection connection) {
        this.connection = connection;
    }

    public Constructor sendCode(String phone_number, int sms_type, int api_id, String api_hash) throws Exception {

        Method auth_sendCode = BookManager.getBook().getMethodByName("auth.sendCode").clone();
            auth_sendCode.getParamByName("phone_number").setData(phone_number);
            auth_sendCode.getParamByName("sms_type").setData(sms_type);
            auth_sendCode.getParamByName("api_id").setData(api_id);
            auth_sendCode.getParamByName("api_hash").setData(api_hash);

        ConstructorPredicateFilter filter = new ConstructorPredicateFilter("rpc_result");

        ConstructorCollector collector = connection.createConstructorCollector(filter);

        connection.sendPacket(auth_sendCode.toData().toEncryptedMessage().toPacket());
        Constructor response = collector.nextResult(MTPConfiguration.getPacketReplyTimeout());


        collector.cancel();

        return response;

    }


    public Constructor signIn(String phone_number, String phone_code_hash, String phone_code) throws Exception {

        Method auth_signIn = BookManager.getBook().getMethodByName("auth.signIn").clone();
        auth_signIn.getParamByName("phone_number").setData(phone_number);
        auth_signIn.getParamByName("phone_code_hash").setData(phone_code_hash);
        auth_signIn.getParamByName("phone_code").setData(phone_code);

        ConstructorFilter[] cf = new ConstructorPredicateFilter[2];
        cf[0] = new ConstructorPredicateFilter("rpc_result");
        cf[1] = new ConstructorPredicateFilter("rpc_error");

        OrFilter filter = new OrFilter(cf);

        ConstructorCollector collector = connection.createConstructorCollector(filter);

        connection.sendPacket(auth_signIn.toData().toEncryptedMessage().toPacket());
        Constructor response = collector.nextResult(MTPConfiguration.getPacketReplyTimeout());


        collector.cancel();

        return response;

    }

    public Constructor signUp (String phone_number, String phone_code_hash, String phone_code,
        String first_name, String last_name) throws Exception {

        Method auth_signUp = BookManager.getBook().getMethodByName("auth.signIn").clone();
        auth_signUp.getParamByName("phone_number").setData(phone_number);
        auth_signUp.getParamByName("phone_code_hash").setData(phone_code_hash);
        auth_signUp.getParamByName("phone_code").setData(phone_code);
        auth_signUp.getParamByName("first_name").setData(first_name);
        auth_signUp.getParamByName("last_name").setData(last_name);

        ConstructorFilter[] cf = new ConstructorPredicateFilter[2];
        cf[0] = new ConstructorPredicateFilter("rpc_result");
        cf[1] = new ConstructorPredicateFilter("rpc_error");

        OrFilter filter = new OrFilter(cf);

        ConstructorCollector collector = connection.createConstructorCollector(filter);

        connection.sendPacket(auth_signUp.toData().toEncryptedMessage().toPacket());
        Constructor response = collector.nextResult(MTPConfiguration.getPacketReplyTimeout());


        collector.cancel();

        return response;

    }





}
