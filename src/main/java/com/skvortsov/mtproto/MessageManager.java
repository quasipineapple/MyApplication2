package com.skvortsov.mtproto;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by сергей on 25.08.13.
 */
public class MessageManager {

    private static final String TAG = "MessageManager";

    public static Message parse (byte[] b){


        Message m = new Message();
        m.setAuth_key_id(Arrays.copyOfRange(b, 0, 8));
        m.setMessage_id(Arrays.copyOfRange(b, 8, 16));
        m.setMessage_data_length(Arrays.copyOfRange(b, 16, 20));
        m.setMessage_data(Arrays.copyOfRange(b, 20, b.length));

        Log.i(TAG, "p.getPayload().length = " + b.length);

        return m;

    }
}
