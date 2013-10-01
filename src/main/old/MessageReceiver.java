package com.skvortsov.mtproto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.skvortsov.mtproto.services.NotificationService;

import java.security.NoSuchAlgorithmException;

/**
 * Created by сергей on 20.08.13.
 */
public class MessageReceiver extends BroadcastReceiver {

    public static final String TAG = "MessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extra = intent.getExtras();
        assert extra != null;

        String action = intent.getAction();
        assert action != null;

        if (action.equals(NotificationService.TAKE_MESSAGE)) {
            try {
                Data d = EncryptedMessageManager.parse(intent.getByteArrayExtra(Packet.PACKET)).toData();
                if(d.isMsgContainer()){


                    for(Constructor c : d.toConstructorArray()){
                        Log.i(TAG, c.toString());
                    }

                }else{
                    Constructor answer = d.toConstructor();
                    Log.i(TAG, answer.toString());
                    if(d.toConstructor().getPredicate().equals("bad_server_salt")){
                        SessionManager.getS().setSalt(
                                Helpers.LongTobyteArray(
                                        (Long)answer.getParamByName("new_server_salt").getData()
                                )
                        );

                    }
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

}





}
