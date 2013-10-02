package com.skvortsov.mtproto;

import java.util.Arrays;

/**
 * Created by сергей on 21.08.13.
 */
public class PacketManager {

    private static String TAG = "PacketManager";

    public static Packet parse (byte[] answer){
        Packet p = new Packet();
        p.setLength(Arrays.copyOfRange(answer, 0, 4));
        p.setNum(Arrays.copyOfRange(answer, 4, 8));
        p.setPayload(Arrays.copyOfRange(answer, 8, answer.length - 4));
        p.setCrc32(Arrays.copyOfRange(answer, answer.length - 4, answer.length));

        return p;
    }


}
