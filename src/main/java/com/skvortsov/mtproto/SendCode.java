package com.skvortsov.mtproto;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skvortsov.mtproto.app.IMControl;



public class SendCode extends Activity {


    private static final String LOG_TAG = "SendCode";

    private ProgressDialog progressDialog;
    private IMControl control;
    private AccountInfo account;


    private EditText tel1Text;
    private EditText tel2Text;
    private ServerProvider serverProvider;
    private Button sendCodeButton;

    public static final int NEW_AUTH_KEY_ID = Menu.FIRST;
    public static final int SERVER_CONFIG_ID = Menu.FIRST + 1;
    public static final int EXIT_APP_ID = Menu.FIRST + 2;



    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendcode_screen);
        setTitle(LOG_TAG);

        serverProvider = new ServerProvider(this);
        SessionManager.init(this);
        SessionManager.readSession();
        BookManager.BuildBook(getResources().openRawResource(R.raw.json));

        if(SessionManager.getS().isAuthKeyOk()){
            Log.i(LOG_TAG, "auth_key loaded from preferences.");
        }else{
            Log.i(LOG_TAG, "Generating new Auth key...");

            new AuthKeyGenerator().execute();
        }

        sendCodeButton = (Button) findViewById(R.id.button222);
        sendCodeButton.setOnClickListener(sendCodeButtonClickListener);



        tel1Text = (EditText) findViewById(R.id.tel1);
        tel2Text = (EditText) findViewById(R.id.tel2);
        control = IMControl.getInstance();
        control.setServer(serverProvider.getServerInfo());

        connectAction();



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        serverProvider.closeConnection();
    }

    private Button.OnClickListener sendCodeButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){
            sendCodeAction();
        }
    };

    private void connectAction(){
        //progressDialog = ProgressDialog.show(SendCode.this, null, getResources()
        //        .getString(R.string.sendCodeProgressMessage), true);
        new Thread(){
            @Override
            public void run() {
                control.connect();
        //        progressDialog.dismiss();
            }
        }.start();

    }

    private void sendCodeAction(){

        //progressDialog = ProgressDialog.show(SendCode.this, null, getResources()
        //       .getString(R.string.sendCodeProgressMessage), true);


        new Thread(){

            @Override
            public void run() {

                String phone_code = tel1Text.getText().toString().trim();
                String phone = tel2Text.getText().toString().trim();
                String phone_number = phone_code.concat(phone);

                account = control.getAccount();
                account.setPhone_number("+79056624155");

                String auth_sendCodeResult = control.auth_sendCode();

                if(auth_sendCodeResult.equals("true") || auth_sendCodeResult.equals("false")){
                    Intent intent = new Intent();
                    intent.setClass(SendCode.this, SignIn.class);
                    startActivity(intent);

                }else{
                    //Toast.makeText(SendCode.this, auth_sendCodeResult, Toast.LENGTH_LONG).show();

                    Log.v(LOG_TAG, auth_sendCodeResult);
                }
                //progressDialog.dismiss();

            }
        }.start();


    }


	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		

         menu.add(0, SERVER_CONFIG_ID, 0, R.string.server_config);
		 menu.add(0, NEW_AUTH_KEY_ID, 0, R.string.new_auth_key);
         menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);


		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {

	    	case EXIT_APP_ID:
	    		finish();
	    		return true;
            case NEW_AUTH_KEY_ID:
                new AuthKeyGenerator().execute();
                return true;
            case SERVER_CONFIG_ID:
                Intent ii = new Intent(SendCode.this, ServerConfig.class);
                startActivity(ii);
                return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

}