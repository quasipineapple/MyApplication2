package com.skvortsov.mtproto;

/**
 * Created by сергей on 12.08.13.
 */
public class Session {

    private byte[] auth_key_id;
    private byte[] session_id;
    private byte[] auth_key;
    private byte[] server_nonce;
    private byte[] new_nonce;
    private byte[] salt;

    private int counter;
    private int seq_no;

    public Session(){

        counter = 0;
    }


    public int getSeq_no(){
        return seq_no;
    }

    public void setSeq_no(int i){

        seq_no = i;
    }

    public Boolean isAuthKeyOk(){
        return getAuth_key().length == 256;
    }

    public byte[] getAuth_key() {
        return auth_key;
    }

    public void setAuth_key(byte[] bb) {
        auth_key = bb;
    }

    public byte[] getServer_nonce() {
        return server_nonce;
    }

    public void setServer_nonce(byte[] b) {
        server_nonce = b;
    }

    public byte[] getNew_nonce() {
        return new_nonce;
    }

    public void setNew_nonce(byte[] b) {
        new_nonce = b;
    }

    public byte[] getSession_id() {
        return session_id;
    }

    public void setSession_id(byte[] b) {
        session_id = b;

    }

    public byte[] getAuth_key_id() {
        return auth_key_id;
    }

    public void setAuth_key_id(byte[] b) {
        auth_key_id = b;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] b) {
        salt = b;
    }

    public int getCounter() {
        return counter++;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getNextSeqNo() {

        //return 0;
        if(seq_no%2==0){
            return seq_no+=1;
        }else{
            return seq_no+=2;
        }

    }
}
