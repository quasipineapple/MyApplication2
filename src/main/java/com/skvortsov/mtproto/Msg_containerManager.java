package com.skvortsov.mtproto;

import java.util.Arrays;

/**
 * Created by сергей on 23.08.13.
 */
public class Msg_containerManager {

    public static Msg_container parse (byte[] bytes) {

        Msg_container mgc = new Msg_container();
        mgc.setConstructor(Arrays.copyOfRange(bytes, 0, 4));
        mgc.setCount_elements(Arrays.copyOfRange(bytes, 4, 8));
        mgc.setMsgs(MsgManager.parse(Arrays.copyOfRange(bytes, 8, bytes.length), mgc.getCount_elements()));

        return mgc;
    }

}
