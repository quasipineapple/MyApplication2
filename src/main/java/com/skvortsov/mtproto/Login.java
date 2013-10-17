package com.skvortsov.mtproto;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.skvortsov.mtproto.app.IMControl;
import com.skvortsov.mtproto.interfaces.ConstructorFilter;
import com.skvortsov.mtproto.interfaces.IAppManager;


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
    public static final int NEW_AUTH_KEY_ID = Menu.FIRST + 1;
    public static final int SERVER_CONFIG_ID = Menu.FIRST + 2;
    public static final int EXIT_APP_ID = Menu.FIRST + 3;

    private Button.OnClickListener loginButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){
            loginAction();
        }
    };

    private OnClickListener myButtonClickListener = new Button.OnClickListener(){
        public void onClick(View v){

        }
    };
    private ProgressDialog progressDialog;
    private IMControl control;
    private ServerProvider serverProvider;


    /** Called when the activity is first created. */	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        setTitle("Login");

        serverProvider = new ServerProvider(this);
        SessionManager.init(this);
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
        control = IMControl.getInstance();
        control.setServer(serverProvider.getServerInfo());

        connectAction();



    }



    private void connectAction(){
        progressDialog = ProgressDialog.show(Login.this, null, getResources()
                .getString(R.string.loginProgressMessage), true);
        new Thread(){
            @Override
            public void run() {
                control.connect();
                progressDialog.dismiss();
            }
        }.start();

    }

    private void loginAction(){

        //progressDialog = ProgressDialog.show(Login.this, null, getResources()
        //        .getString(R.string.loginProgressMessage), true);

        new Thread(){

            @Override
            public void run() {
                String loginResult = control.login();
                //Auth.SendCode(phone, null, null);
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
		
		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
         menu.add(0, SERVER_CONFIG_ID, 0, R.string.server_config);
		 menu.add(0, NEW_AUTH_KEY_ID, 0, R.string.new_auth_key);
         menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);


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
	    		finish();
	    		return true;
            case NEW_AUTH_KEY_ID:
                new AuthKeyGenerator().execute();
                return true;
            case SERVER_CONFIG_ID:
                Intent ii = new Intent(Login.this, ServerConfig.class);
                startActivity(ii);
                return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

}