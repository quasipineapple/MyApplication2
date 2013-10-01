package com.skvortsov.mtproto;

import java.util.List;

/**
 * Created by сергей on 23.08.13.
 */
public class Msg_container {

    private static final String TAG = "Msg_container";
    private byte[] constructor;
    private byte[] count_elements;
    private List<Msg> msgs;


    public byte[] getConstructor() {
        return constructor;
    }

    public void setConstructor(byte[] constructor) {
        this.constructor = constructor;
    }

    public byte[] getCount_elements() {
        return count_elements;
    }

    public void setCount_elements(byte[] count_elements) {
        this.count_elements = count_elements;
    }

    public List<Msg> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<Msg> msgs) {
        this.msgs = msgs;
    }
}
