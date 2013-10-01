package com.skvortsov.mtproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Created by сергей on 12.08.13.
 */
public class EncryptedMessage {
    private static final String TAG = "EncryptedMessage";

    private byte[] auth_key_id;
    private byte[] msgKey;
    private byte[] encrypted_data;

    public EncryptedMessage(){}


    public byte[] getEncrypted_data() {
        return encrypted_data;
    }

    public void setEncrypted_data(byte[] encrypted_data) {
        this.encrypted_data = encrypted_data;
    }

    public byte[] getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(byte[] msgKey) {
        this.msgKey = msgKey;
    }

    public byte[] getAuth_key_id() {
        return auth_key_id;
    }

    public void setAuth_key_id(byte[] auth_key_id) {
        this.auth_key_id = auth_key_id;
    }

    public int length() {
        return auth_key_id.length + msgKey.length + encrypted_data.length;

    }

    public byte[] array() {

        return ByteBuffer.allocate(length()).put(auth_key_id).put(msgKey).put(encrypted_data).array();
    }

    public Data toData() throws NoSuchAlgorithmException {
        Data d = new Data();

        byte[] decrypted_answer = CryptoUtils.decrypt_data(this.getEncrypted_data(), SessionManager.getS().getAuth_key(), this.getMsgKey());

        d.setSalt(Arrays.copyOfRange(decrypted_answer, 0, 8));
        d.setSession_id(Arrays.copyOfRange(decrypted_answer, 8, 16));
        d.setMessage_id(Arrays.copyOfRange(decrypted_answer, 16, 24));
        d.setSeq_no(Arrays.copyOfRange(decrypted_answer, 24, 28));
        d.setMessage_data_length(Arrays.copyOfRange(decrypted_answer, 28, 32));

        byte[] b_length_answer = Helpers.conv_from_LE_to_BE(d.getMessage_data_length());
        int i_length = ByteBuffer.wrap(b_length_answer).getInt();

        d.setMessage_data(Arrays.copyOfRange(decrypted_answer, 32, 32 + i_length));
        d.setPadding(Arrays.copyOfRange(decrypted_answer, 32 + i_length, decrypted_answer.length));


        return d;
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
}
