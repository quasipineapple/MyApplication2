/* 
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skvortsov.mtproto.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.skvortsov.mtproto.BookManager;
import com.skvortsov.mtproto.Constructor;
import com.skvortsov.mtproto.Data;
import com.skvortsov.mtproto.EncryptedMessageManager;
import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.Login;
import com.skvortsov.mtproto.Method;
import com.skvortsov.mtproto.Msg;
import com.skvortsov.mtproto.SessionManager;
import com.skvortsov.mtproto.communication.MTPConnection;
import com.skvortsov.mtproto.communication.SocketOperator;
import com.skvortsov.mtproto.interfaces.IAppManager;
import com.skvortsov.mtproto.interfaces.ISocketOperator;
import com.skvortsov.mtproto.interfaces.IUpdateData;
import com.skvortsov.mtproto.mtp_api.Auth;
import com.skvortsov.mtproto.tools.LocalStorageHandler;
import com.skvortsov.mtproto.types.FriendInfo;
import com.skvortsov.mtproto.types.MessageInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IMService extends Service implements IAppManager, IUpdateData {
    private static final String TAG = "IMService";

	public ConnectivityManager conManager = null;

    private MTPConnection connection;

	ISocketOperator socketOperator;

	private final IBinder mBinder = new IMBinder();

	private boolean authenticatedUser = false;

	private LocalStorageHandler localstoragehandler;

	private NotificationManager mNM;
    private String tel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println(TAG +  "ImService started");
        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public void updateData(MessageInfo[] messages, FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey) {

    }

    public class IMBinder extends Binder {
		public IAppManager getService() {
			return IMService.this;
		}

	}

    @Override
    public void onCreate()
    {
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

         localstoragehandler = new LocalStorageHandler(this);
        // Display a notification about us starting.  We put an icon in the status bar.
     //   showNotification();
    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        try {
            socketOperator =  new SocketOperator();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new LocalStorageHandler(this);

    }


	@Override
	public IBinder onBind(Intent intent)
	{
		return mBinder;
	}


    @Override
    public String help_getConfig() throws CloneNotSupportedException, NoSuchAlgorithmException, IOException {

        Method help_getConfig = BookManager.getBook().getMethodByName("help.getConfig").clone();
        byte[] answer = socketOperator.sendHttpRequest(
                help_getConfig.toData().toEncryptedMessage().array());

        Data d = EncryptedMessageManager.parse2(answer).toData();

        //System.out.println(Helpers.byteArrayToHex(help_getConfig.toData().array()));
        //System.out.println(Helpers.byteArrayToHex(help_getConfig.toData().toEncryptedMessage().array()));
        getAnswer(d);
        return Login.GET_CONFIG_OK;
    }

    @Override
    public String auth_sendCode(String tel) throws IOException, CloneNotSupportedException, NoSuchAlgorithmException {
        this.tel = tel;
        this.authenticatedUser = false;

        if(!SessionManager.getS().isAuthKeyOk()) {
            Log.d(TAG, "Неверный auth_key. Перезапустите приложение.");
            return Login.AUTHENTICATION_FAILED;
        }

        Auth.SendCode();

        

        //Constructor ping = BookManager.getBook().getConstructorByPredicate("ping").clone();
        //ping.getParamByName("ping_id").setData((long) 1);



        byte[] answer = socketOperator.sendHttpRequest(
                auth_sendCode.toData().toEncryptedMessage().array());
        /*byte[] answer = socketOperator.sendHttpRequest(
                ping.toData().toEncryptedMessage().array());*/
        Data d = EncryptedMessageManager.parse2(answer).toData();

        /*byte[] answer = socketOperator.sendPacket(
                ping.toData().toEncryptedMessage().toPacket().array());
        Data d = EncryptedMessageManager.parse(answer).toData();*/

        //System.out.println(Helpers.byteArrayToHex(ping.toData().array()));
        //System.out.println(Helpers.byteArrayToHex(ping.toData().toEncryptedMessage().array()));
        getAnswer(d);
        //Log.d(TAG, Helpers.byteArrayToHex(answer));
        //imService.sendPacket(auth_sendCode.toData().toEncryptedMessage().toPacket());

        return Login.AUTHENTICATION_OK;
    }

    @Override
    public String auth_signIn(String tel) throws IOException, CloneNotSupportedException, NoSuchAlgorithmException {


        return null;
    }


    private void getAnswer(Data d) throws IOException, CloneNotSupportedException, NoSuchAlgorithmException {

        if(d.isMsgContainer()){
            List<Msg> msg_need_ack = new ArrayList<Msg>();

            Long msgc_id = ByteBuffer.wrap(d.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
            System.out.println("MsgContainer (" + msgc_id + ") (" + msg_need_ack.size() +") begin; ");
            for(Msg m: d.toMsgArray()){

                Long m_id = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
                Constructor c = m.toConstructor();

                System.out.println("Msg (" + m_id + ") " + c.toString());
                if(need_ack(m)){
                    msg_need_ack.add(m);
                }
            }

            System.out.println("MsgContainer (" + msgc_id + ") end;");
            System.out.println("need ack for " + msg_need_ack.size() + " messages;");


            send_msgs_ack(msg_need_ack);


        }else{
            Constructor c = d.toConstructor();
            System.out.println(c.toString());
            if(c.getPredicate().equals("bad_server_salt")){
                SessionManager.getS().setSalt(
                        Helpers.LongTobyteArray(
                                (Long)c.getParamByName("new_server_salt").getData()
                        )
                );

            }
        }
    }

    private void send_msgs_ack(List<Msg> mm) throws CloneNotSupportedException, NoSuchAlgorithmException, IOException {

        Constructor msgs_ack = BookManager.getBook().getConstructorByPredicate("msgs_ack").clone();
        Long[] msgs_ids = new Long[mm.size()];
        int i = 0;
        //StringBuilder sb = new StringBuilder();

        for(Msg m : mm){
            msgs_ids[i++] = ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong();
            //sb.append(ByteBuffer.wrap(m.getMsg_id()).order(ByteOrder.LITTLE_ENDIAN).getLong());
            //sb.append(";");
        }

        msgs_ack.getParamByName("msg_ids").setData(msgs_ids);
        Data dd = msgs_ack.toData();

        System.out.println("send_msgs_ack(" +
                ByteBuffer.wrap(dd.getMessage_id()).order(ByteOrder.LITTLE_ENDIAN).getLong() + ") " +
                "for msg_ids " + Arrays.toString(msgs_ids));

        socketOperator.sendHttpRequest(
                dd.toEncryptedMessage().array());
        //Data dd2 = EncryptedMessageManager.parse2(answer).toData();


        //getAnswer(dd2);

    }

    private boolean need_ack(Msg m) throws CloneNotSupportedException {

        //if(m.toConstructor().getPredicate().equals("bad_msg_notification")){

        //    return false;
        //}
        return true;

    }

    @Override
    public String signUpUser(String usernameText, String passwordText, String email) {
        /*String params = "username=" + usernameText +
                "&password=" + passwordText +
                "&action=" + "signUpUser"+
                "&email=" + emailText+
                "&";

        String result = socketOperator.sendHttpRequest(params);*/

        return "";
    }

    @Override
	public void onDestroy() {
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	


    @Override
    public boolean isNetworkConnected() {
        return conManager.getActiveNetworkInfo().isConnected();
     }


}