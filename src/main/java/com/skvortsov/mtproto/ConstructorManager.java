package com.skvortsov.mtproto;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by сергей on 22.08.13.
 */
public class ConstructorManager {

    private static final String TAG = "ConstructorManager";

    public static void ProcessConstructor(Constructor c) {
        if(c.getPredicate().equals("bad_server_salt")){
            SessionManager.getS().setSalt(
                    ByteBuffer.allocate(8).order(
                            ByteOrder.LITTLE_ENDIAN).putLong(
                            Long.valueOf(c.getParamByName("new_server_salt").getData().toString())
                    ).array()
            );
        }
        Log.i(TAG, "Получено сообщение: " + c.toString());
    }

}
