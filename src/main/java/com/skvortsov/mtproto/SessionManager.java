package com.skvortsov.mtproto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by сергей on 21.08.13.
 */
public class SessionManager {

    private static final String KEY_AUTH_KEY = "auth_key";
    private static final String KEY_AUTH_KEY_ID = "auth_key_id";
    private static final String KEY_SERVER_NONCE = "server_nonce";
    private static final String KEY_NEW_NONCE = "new_nonce";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_SALT = "salt";
    private static final String KEY_MSG_SEQNO = "msg_seqno";
    private static final String KEY_DEF_VAL = "00";
    private static Session s;
    private static SharedPreferences mPreferences;

    public static void init(Context context) {
        s = new Session();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void saveSession(){

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KEY_AUTH_KEY, Helpers.bytesToHex(s.getAuth_key()));
        editor.putString(KEY_SERVER_NONCE, Helpers.bytesToHex(s.getServer_nonce()));
        editor.putString(KEY_NEW_NONCE, Helpers.bytesToHex(s.getNew_nonce()));
        editor.putString(KEY_AUTH_KEY_ID, Helpers.bytesToHex(s.getAuth_key_id()));
        editor.putString(KEY_SESSION_ID, Helpers.bytesToHex(s.getSession_id()));
        editor.putString(KEY_SALT, Helpers.bytesToHex(s.getSalt()));
        editor.putInt(KEY_MSG_SEQNO, s.getSeq_no());
        editor.commit();
    }

    public static void saveSeq_no(){

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(KEY_MSG_SEQNO, s.getSeq_no());
        editor.commit();
    }

    public static void readSession(){

        if(mPreferences != null){
            s.setAuth_key(Helpers.hexStringToByteArray(mPreferences.getString(KEY_AUTH_KEY, KEY_DEF_VAL)));
            s.setServer_nonce(Helpers.hexStringToByteArray(mPreferences.getString(KEY_SERVER_NONCE, KEY_DEF_VAL)));
            s.setNew_nonce(Helpers.hexStringToByteArray(mPreferences.getString(KEY_NEW_NONCE, KEY_DEF_VAL)));
            s.setAuth_key_id(Helpers.hexStringToByteArray(mPreferences.getString(KEY_AUTH_KEY_ID, KEY_DEF_VAL)));
            s.setSession_id(Helpers.hexStringToByteArray(mPreferences.getString(KEY_SESSION_ID, KEY_DEF_VAL)));
            s.setSalt(Helpers.hexStringToByteArray(mPreferences.getString(KEY_SALT, KEY_DEF_VAL)));
            s.setSeq_no(mPreferences.getInt(KEY_MSG_SEQNO, 0));

        }else{
            throw new RuntimeException("read session failed");
        }

    }

    public static Session getS() {
        return s;
    }

    public static void setS(Session s) {
        SessionManager.s = s;
    }
}
