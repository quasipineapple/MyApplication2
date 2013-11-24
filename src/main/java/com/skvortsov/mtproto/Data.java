package com.skvortsov.mtproto;

import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;

/**
 * Created by сергей on 21.08.13.
 */
public class Data {

    private static final String TAG = "Data";
    private byte[] salt;
    private byte[] session_id;
    private byte[] message_id;
    private byte[] seq_no;
    private byte[] message_data_length;
    private byte[] message_data;
    private byte[] padding;


    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getSession_id() {
        return session_id;
    }

    public void setSession_id(byte[] session_id) {
        this.session_id = session_id;
    }

    public byte[] getMessage_id() {
        return message_id;
    }

    public void setMessage_id(byte[] message_id) {
        this.message_id = message_id;
    }

    public byte[] getSeq_no() {
        return seq_no;
    }

    public void setSeq_no(byte[] seq_no) {
        this.seq_no = seq_no;
    }

    public byte[] getMessage_data_length() {
        return message_data_length;
    }

    public void setMessage_data_length(byte[] message_data_length) {
        this.message_data_length = message_data_length;
    }

    public byte[] getMessage_data() {
        return message_data;
    }

    public void setMessage_data(byte[] message_data) {
        this.message_data = message_data;
    }

    public int length() {
        return salt.length + session_id.length + message_id.length + seq_no.length + message_data_length.length + message_data.length;
    }

    public byte[] array(){
        return ByteBuffer.allocate(length()).put(salt).put(session_id).put(message_id).put(seq_no).put(message_data_length).put(message_data).array();
    }

    public byte[] getPadding() {
        return padding;
    }

    public void setPadding(byte[] padding) {
        this.padding = padding;
    }

    public boolean isMsgContainer() throws CloneNotSupportedException {
        Constructor c = BookManager.getBook().getConstructorById(
                String.valueOf(
                        ByteBuffer.wrap(
                                this.getMessage_data()).order(
                                ByteOrder.LITTLE_ENDIAN).getInt()
                )).clone();

        return c.getPredicate().equals("msg_container");
    }

    public EncryptedMessage toEncryptedMessage() throws NoSuchAlgorithmException {
        EncryptedMessage em = new EncryptedMessage();

        em.setAuth_key_id(SessionManager.getS().getAuth_key_id());
        //2) msgKey
        byte[] mkey = new BigInteger(1, Helpers.substr(Helpers.SHA1(this.array()), 4, 20)).toByteArray();
        //byte[] mkey = Arrays.copyOfRange(Helpers.SHA1(this.array()), 4, 20);

        if (mkey[0] == 0) {
            Log.w(TAG, "mkey[0] == 0");
            byte[] tmp = new byte[mkey.length - 1];
            System.arraycopy(mkey, 1, tmp, 0, tmp.length);
            mkey = tmp;
        }
        em.setMsgKey(ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).put(mkey).array());
        //em.setMsgKey(ByteBuffer.allocate(16).put(mkey).array());

        em.setEncrypted_data(CryptoUtils.encrypt_data(this, SessionManager.getS().getAuth_key(), em.getMsgKey()));
        //em.setEncrypted_data(CryptoUtils.encrypt_data(this, SessionManager.getS().getAuth_key(), mkey));

        return em;
    }

    public Constructor toConstructor() throws CloneNotSupportedException {
        //Log.i(TAG, Helpers.byteArrayToHex(this.getMessage_data()));
        return SerializationUtils.deserialize(this.getMessage_data());
    }



    public Msg[] toMsgArray() {
        //Log.i(TAG, Helpers.byteArrayToHex(this.getMessage_data()));
        Msg_container msg_container = Msg_containerManager.parse(this.getMessage_data());
        int bl = ByteBuffer.wrap(msg_container.getCount_elements()).getInt();
        //Log.w(TAG, String.valueOf(bl));
        Msg[] mm = new Msg[bl];
        int i = 0;
        //Log.w(TAG, String.valueOf(msgc.getMsgs().size()));
        for(Msg m : msg_container.getMsgs()){
            mm[i] = m;
            i++;
        }
        return mm;
    }

}
