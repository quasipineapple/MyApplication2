package com.skvortsov.mtproto;

import java.nio.ByteBuffer;

/**
 * Created by сергей on 23.08.13.
 */
public class Msg {

    private byte[] msg_id;
    private byte[] seq_no;
    private byte[] body_length;
    private byte[] body;

    public byte[] getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(byte[] msg_id) {
        this.msg_id = msg_id;
    }

    public byte[] getSeq_no() {
        return seq_no;
    }

    public void setSeq_no(byte[] seq_no) {
        this.seq_no = seq_no;
    }

    public byte[] getBody_length() {
        return body_length;
    }

    public void setBody_length(byte[] body_length) {
        this.body_length = body_length;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] array(){
        return ByteBuffer.allocate(length()).put(msg_id).put(seq_no).put(body_length).put(body).array();
    }

    public int length(){
        return msg_id.length + seq_no.length + body_length.length + body.length;
    }

    public Constructor toConstructor() throws CloneNotSupportedException {
        //Log.i(TAG, Helpers.byteArrayToHex(this.getMessage_data()));
        return SerializationUtils.deserialize(this.getBody());
    }
}
