package com.skvortsov.mtproto.mtp_api;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.Method;

/**
 * Created by skvortsov on 9/21/13.
 */
public class Auth {

    public Method SendCode() throws CloneNotSupportedException {

        Method auth_sendCode = BookManager.getBook().getMethodByName("Auth.sendCode").clone();
        auth_sendCode.getParamByName("phone_number").setData("+79056624155");
        auth_sendCode.getParamByName("sms_type").setData(0);
        auth_sendCode.getParamByName("api_id").setData(1463);
        auth_sendCode.getParamByName("api_hash").setData("437a55dcaee748fc1596a1bb5e6ca7db");

        return auth_sendCode;
    }
}
