package com.skvortsov.mtproto;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by сергей on 21.08.13.
 */
public class EncryptedMessageManager {

    private static String TAG = "EncryptedMessageBuilder";

    public static EncryptedMessage parse (byte[] b){

        Packet p = PacketManager.parse(b);
        EncryptedMessage em = new EncryptedMessage();
        em.setAuth_key_id(Arrays.copyOfRange(p.getPayload(), 0, 8));
        em.setMsgKey(Arrays.copyOfRange(p.getPayload(), 8, 24));
        em.setEncrypted_data(Arrays.copyOfRange(p.getPayload(), 24, p.getPayload().length));
        Log.i(TAG, "p.getPayload().length" + p.getPayload().length);

        return em;

    }

    public static EncryptedMessage parse2 (byte[] b){


        EncryptedMessage em = new EncryptedMessage();
        em.setAuth_key_id(Arrays.copyOfRange(b, 0, 8));
        em.setMsgKey(Arrays.copyOfRange(b, 8, 24));
        em.setEncrypted_data(Arrays.copyOfRange(b, 24, b.length));
        Log.i(TAG, "p.getPayload().length " + b.length);

        return em;

    }





}
