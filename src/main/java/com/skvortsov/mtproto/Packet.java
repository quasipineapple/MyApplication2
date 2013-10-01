package com.skvortsov.mtproto;


import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by skvortsov on 06.08.13.
 */
public class Packet {

    public static final String TAG = "Packet";

    private byte[] length;
    private byte[] num;
    private byte[] payload;
    private byte[] crc32;

    public Packet(){}

    public byte[] array() {
        return  ByteBuffer.allocate(length()).put(length).put(num).put(payload).put(crc32).array();
    }

    public int length() {
        return length.length + num.length + payload.length + crc32.length;
    }

    public byte[] getLength() {
        return length;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    public byte[] getCrc32() {
        return crc32;
    }

    public void setCrc32(byte[] crc32) {
        this.crc32 = crc32;
    }

    public byte[] getNum() {
        return num;
    }

    public void setNum(byte[] num) {
        this.num = num;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Message toMessage(){
        Message m = new Message();
        m.setAuth_key_id(Arrays.copyOfRange(this.getPayload(), 0, 8));
        m.setMessage_id(Arrays.copyOfRange(this.getPayload(), 8, 16));
        m.setMessage_data_length(Arrays.copyOfRange(this.getPayload(), 16, 20));
        m.setMessage_data(Arrays.copyOfRange(this.getPayload(), 20, payload.length));
        Log.w(TAG, "message data length = " + Helpers.byteArrayToHex(m.getMessage_data_length()));
        Log.w(TAG, "payload length = " + payload.length);
        return m;
    }
}
