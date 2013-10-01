package com.skvortsov.mtproto;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skvortsov.mtproto.interfaces.IAppManager;
import com.skvortsov.mtproto.services.IMService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class Login extends Activity {	

    protected static final int NOT_CONNECTED_TO_SERVICE = 0;
	protected static final int FILL_BOTH_USERNAME_AND_PASSWORD = 1;
	public static final String AUTHENTICATION_FAILED = "0";
    public static final String AUTHENTICATION_OK = "";
    public static final String GET_CONFIG_OK = "";
	public static final String FRIEND_LIST = "FRIEND_LIST";
	protected static final int MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT = 2 ;
	protected static final int NOT_CONNECTED_TO_NETWORK = 3;
    private static final String TAG = "Login";
    private EditText tel1Text;
    private EditText tel2Text;

    private IAppManager imService;
    public static final int SIGN_UP_ID = Menu.FIRST;
    public static final int EXIT_APP_ID = Menu.FIRST + 1;
    public static final int NEW_AUTH_KEY_ID = Menu.FIRST + 2;

    private Button.OnClickListener loginButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){
            auth_sendCode();
        }
    };

    private OnClickListener myButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){
            help_GetConfig();
        }
    };

    private void help_GetConfig() {
        Thread myThread = new Thread(){
            private Handler handler = new Handler();
            @Override
            public void run() {
                String result = null;
                try {
                    result = imService.help_getConfig();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (result == null || result.equals(AUTHENTICATION_FAILED))
                {
                    /*
                     * Authenticatin failed, inform the user
                     */
                    handler.post(new Runnable(){
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.make_sure_username_and_password_correct,
                                    Toast.LENGTH_LONG).show();
                            //showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
                        }
                    });

                }
                else {
                    handler.post(new Runnable(){
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.qwe,
                                    Toast.LENGTH_LONG).show();
                            //showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
                        }
                    });
                }

            }
        };
        myThread.start();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            imService = ((IMService.IMBinder)service).getService();  
            

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	imService = null;
            Toast.makeText(Login.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };



    /** Called when the activity is first created. */	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        setTitle("Login");

        /*
         * Start and bind the  imService 
         **/
    	startService(new Intent(Login.this,  IMService.class));
        SessionManager.readSession();
        BookManager.BuildBook(getResources().openRawResource(R.raw.json));

        if(SessionManager.getS().isAuthKeyOk()){
            Log.i(TAG, "auth_key loaded from preferences.");
        }else{
            Log.i(TAG, "Generating new Auth key...");

            new AuthKeyGenerator().execute();
        }
        

        
        Button loginButton = (Button) findViewById(R.id.button222);
        loginButton.setOnClickListener(loginButtonClickListener);

        Button myButton = (Button) findViewById(R.id.button333);
        myButton.setOnClickListener(myButtonClickListener);

        tel1Text = (EditText) findViewById(R.id.tel1);
        tel2Text = (EditText) findViewById(R.id.tel2);


        


    }

    private void auth_sendCode(){

        if (imService == null) {
            Toast.makeText(getApplicationContext(),R.string.not_connected_to_service, Toast.LENGTH_LONG).show();
            //showDialog(NOT_CONNECTED_TO_SERVICE);
        }
        else if (!imService.isNetworkConnected())
        {
            Toast.makeText(getApplicationContext(),R.string.not_connected_to_network, Toast.LENGTH_LONG).show();
            //showDialog(NOT_CONNECTED_TO_NETWORK);

        }
        else if (tel1Text.length() <= 0 &&
                tel2Text.length() <= 0)
        {
            Toast.makeText(getApplicationContext(),R.string.fill_both_username_and_password, Toast.LENGTH_LONG).show();
        }

        Thread loginThread = new Thread(){
            private Handler handler = new Handler();
            @Override
            public void run() {
                String result = null;

                try {
                    result = imService.auth_sendCode(tel1Text.getText().toString().concat(tel2Text.getText().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                if (result == null || result.equals(AUTHENTICATION_FAILED))
                {
                    /*
                     * Authenticatin failed, inform the user
                     */
                    handler.post(new Runnable(){
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.make_sure_username_and_password_correct,
                                    Toast.LENGTH_LONG).show();
                            //showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
                        }
                    });

                }
                else {

                    handler.post(new Runnable(){
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    R.string.qwe,
                                    Toast.LENGTH_LONG).show();
                            //showDialog(MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT);
                        }
                    });

                    /*handler.post(new Runnable(){
                        public void run() {
                            Intent i = new Intent(Login.this, SignUp.class);
                            //i.putExtra(FRIEND_LIST, result);
                            startActivity(i);
                            Login.this.finish();
                        }
                    });*/



                }
            }
        };

        loginThread.start();

    }


    @Override
    protected Dialog onCreateDialog(int id) 
    {    	
    	int message = -1;    	
    	switch (id) 
    	{
    		case NOT_CONNECTED_TO_SERVICE:
    			message = R.string.not_connected_to_service;			
    			break;
    		case FILL_BOTH_USERNAME_AND_PASSWORD:
    			message = R.string.fill_both_username_and_password;
    			break;
    		case MAKE_SURE_USERNAME_AND_PASSWORD_CORRECT:
    			message = R.string.make_sure_username_and_password_correct;
    			break;
    		case NOT_CONNECTED_TO_NETWORK:
    			message = R.string.not_connected_to_network;
    			break;
    		default:
    			break;
    	}
    	
    	if (message == -1) 
    	{
    		return null;
    	}
    	else 
    	{
    		return new AlertDialog.Builder(Login.this)       
    		.setMessage(message)
    		.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				/* User clicked OK so do some stuff */
    			}
    		})        
    		.create();
    	}
    }

	@Override
	protected void onPause() 
	{
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{		
		bindService(new Intent(Login.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);
	    		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		
		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
         menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);
		 menu.add(0, NEW_AUTH_KEY_ID, 0, R.string.new_auth_key);



		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case SIGN_UP_ID:
	    		Intent i = new Intent(Login.this, SignUp.class);
	    		startActivity(i);
	    		return true;
	    	case EXIT_APP_ID:
	    		//cancelButton.performClick();
	    		return true;
            case NEW_AUTH_KEY_ID:
                new AuthKeyGenerator().execute();
                return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

}