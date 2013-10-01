package com.skvortsov.mtproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Created by сергей on 04.08.13.
 */
public class Message {

    private static final String TAG = "Message";
    private byte[] auth_key_id;
    private byte[] message_id;
    private byte[] message_data_length;
    private byte[] message_data;

    public Message() {}

    public byte[] array(){
        return ByteBuffer.allocate(length()).put(auth_key_id).put(message_id).put(message_data_length).put(message_data).array();
    }

    public int length(){
        return auth_key_id.length + message_id.length + message_data_length.length + message_data.length;
    }

    public byte[] getMessage_data_length() {
        return message_data_length;
    }

    public void setMessage_data_length(byte[] message_data_length) {
        this.message_data_length = message_data_length;
    }

    public byte[] getAuth_key_id() {
        return auth_key_id;
    }

    public void setAuth_key_id(byte[] auth_key_id) {
        this.auth_key_id = auth_key_id;
    }

    public byte[] getMessage_id() {
        return message_id;
    }

    public void setMessage_id(byte[] message_id) {
        this.message_id = message_id;
    }

    public byte[] getMessage_data() {
        return message_data;
    }

    public void setMessage_data(byte[] message_data) {
        this.message_data = message_data;
    }

    public Packet toPacket(){
        Packet p = new Packet();

        p.setLength(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4 + 4 + this.length() + 4).array());
        p.setNum(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SessionManager.getS().getCounter()).array());
        p.setPayload(this.array());

        Checksum checksum = new CRC32();
        ByteBuffer b = ByteBuffer.allocate(4 + 4 + this.length());
        b.put(p.getLength()).put(p.getNum()).put(p.getPayload());
        checksum.update(b.array(), b.arrayOffset(), b.array().length);
        p.setCrc32(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) checksum.getValue()).array());

        return p;
    }

    public Constructor toConstructor() throws CloneNotSupportedException {
        return SerializationUtils.deserialize(this.getMessage_data());
    }

}
