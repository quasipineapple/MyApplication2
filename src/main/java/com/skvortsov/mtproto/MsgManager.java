package com.skvortsov.mtproto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by сергей on 23.08.13.
 */
public class MsgManager {

    private static final String TAG = "MsgManager";

    public static List<Msg> parse (byte[] bytes, byte[] count) {

        byte[] b_count = Helpers.conv_from_LE_to_BE(count);
        int i_count = ByteBuffer.wrap(b_count).getInt();

        ArrayList<Msg> lmsg = new ArrayList<Msg>();

        for(int i=0;i<i_count;i++){
            Msg m = new Msg();
            m.setMsg_id(Arrays.copyOfRange(bytes, 0, 8));
            m.setSeq_no(Arrays.copyOfRange(bytes, 8, 12));
            m.setBody_length(Arrays.copyOfRange(bytes, 12, 16));
            int bl = ByteBuffer.wrap(Helpers.conv_from_LE_to_BE(m.getBody_length())).getInt();
            m.setBody(Arrays.copyOfRange(bytes, 16, 16 + bl));
            bytes = Helpers.substr(bytes, m.length(), bytes.length);
            lmsg.add(m);
        }
        return lmsg;
    }
}
