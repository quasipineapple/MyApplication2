package com.skvortsov.mtproto.mtp_api;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.Method;

/**
 * Created by skvortsov on 9/21/13.
 */
public class Auth {

    public Method SendCode() throws CloneNotSupportedException {

        Method auth_sendCode = BookManager.getBook().getMethodByName("Auth.sendCode").clone();
        auth_sendCode.getParamByName("phone_number").setData("");
        auth_sendCode.getParamByName("sms_type").setData(0);
        auth_sendCode.getParamByName("api_id").setData(0);
        auth_sendCode.getParamByName("api_hash").setData("");

        return auth_sendCode;
    }
}
