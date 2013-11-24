package com.skvortsov.mtproto;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skvortsov.mtproto.app.IMControl;

/**
 * Created by skvortsov on 10/19/13.
 */
public class SignIn extends Activity{

    private static final String LOG_TAG = "SingIn";
    private IMControl control;

    private AccountInfo accountInfo;
    private EditText phone_code;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_screen);
        setTitle(LOG_TAG);

        phone_code = (EditText) findViewById(R.id.your_code);

        nextButton = (Button) findViewById(R.id.next2);
        nextButton.setOnClickListener(nextButtonClickListener);

        control = IMControl.getInstance();

    }

    private Button.OnClickListener nextButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){
            signInAction();
        }
    };

    private void signInAction(){

        new Thread(){

            @Override
            public void run() {

                String code = phone_code.getText().toString().trim();

                accountInfo = control.getAccount();
                accountInfo.setPhone_code(code);

                String auth_signInResult = control.auth_signIn();

                if(accountInfo.getPhone_registered() && auth_signInResult.equals("auth.authorization")){

                    Intent intent = new Intent();
                    intent.setClass(SignIn.this, SignUp.class);
                    startActivity(intent);

                }else if(!accountInfo.getPhone_registered() && auth_signInResult.contains("PHONE_NUMBER_UNOCCUPIED")){
                    Intent intent = new Intent();
                    intent.setClass(SignIn.this, SignUp.class);
                    startActivity(intent);

                }else{
                    //Toast.makeText(SignIn.this, auth_signInResult, Toast.LENGTH_LONG).show();
                    Log.v(LOG_TAG, auth_signInResult);
                }


            }
        }.start();

    }
}
