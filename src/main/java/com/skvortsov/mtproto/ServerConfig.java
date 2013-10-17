package com.skvortsov.mtproto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by skvortsov on 10/15/13.
 */

public class ServerConfig extends Activity {
    private Button cancelButton;
    private Button submitButton;
    private EditText addressEt;
    private EditText portEt;

    private ServerProvider serverProvider;

    private String LOG_TAG = "ServerConfig";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serverconfig);
        setTitle(getString(R.string.serviceConfigTitle));

        cancelButton = (Button) findViewById(R.id.serverConfigCancelButton);
        submitButton = (Button) findViewById(R.id.serverConfigSubmitButton);
        addressEt = (EditText) findViewById(R.id.address_value);
        portEt = (EditText) findViewById(R.id.port_value);

        serverProvider = new ServerProvider(this);

        getServerInfo();

        dealWithButton();

    }

    private void dealWithButton() {
        cancelButton.setOnClickListener(cancelBtn_Listener);
        submitButton.setOnClickListener(submitBtn_Listener);
    }

    private Button.OnClickListener cancelBtn_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            
            Intent intent = new Intent();
            intent.setClass(ServerConfig.this, Login.class);
            startActivity(intent);
        }
    };

    private Button.OnClickListener submitBtn_Listener = new Button.OnClickListener() {
        public void onClick(View v) {
            String address = addressEt.getText().toString().trim();
            String port = portEt.getText().toString().trim();
            Toast toast = Toast.makeText(ServerConfig.this, "",
                    Toast.LENGTH_LONG);

            if (address.equals("")) {
                toast.setText("Please input server address!");
                toast.show();
                Log.v(LOG_TAG, "Please input server address!");
                return;
            }

            if (port.equals("")) {
                toast.setText("Please input server port!");
                toast.show();
                Log.v(LOG_TAG, "Please input server port!");
                return;
            } else if (port.equals("0")) {
                toast.setText("Server port cannot be 0!");
                toast.show();
                Log.v(LOG_TAG, "Server port cannot be 0!");
                return;
            }

            try {
                serverProvider.updateServerInfo(new Server(address, Integer
                        .parseInt(port)));

                toast.setText("Server configuration updated!");
                toast.show();
                Log.v(LOG_TAG, "Server configuration updated!");
                Intent intent = new Intent();
                intent.setClass(ServerConfig.this, Login.class);
                startActivity(intent);

            } catch (NumberFormatException e) {
                toast.setText("Server port should be Integer!");
                toast.show();
                return;
            }
        }
    };

    /**
     * Get the server information and display while data find
     */
    private void getServerInfo() {
        Server server = serverProvider.getServerInfo();
        if (null != server) {
            addressEt.setText(server.getAddress());
            portEt.setText(Integer.toString(server.getPort()));
            Log.v(LOG_TAG, "getServerInfo " + server.getAddress() + " "
                    + Integer.toString(server.getPort()));
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        serverProvider.closeConnection();
    }
}
