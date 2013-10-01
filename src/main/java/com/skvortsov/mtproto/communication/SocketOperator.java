package com.skvortsov.mtproto.communication;

import android.util.Log;

import com.skvortsov.mtproto.Helpers;
import com.skvortsov.mtproto.interfaces.ISocketOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;


public class SocketOperator implements ISocketOperator
{

	//private static final String AUTHENTICATION_SERVER_ADDRESS = "http://173.240.5.253:443/api"; //TODO change to your WebAPI Address
    private static final String AUTHENTICATION_SERVER_ADDRESS = "http://95.142.192.65:443/api"; //TODO change to your WebAPI Address

	private static final String HTTP_REQUEST_FAILED = null;
    private static final String TAG = "SocketOperator" ;
    InetAddress serverAddr;
    Socket mySock;
    DataOutputStream dos;
    DataInputStream dis;



    public SocketOperator() throws IOException {
        serverAddr = InetAddress.getByName("95.142.192.65");
        mySock = new Socket(serverAddr, 443);
        dos = new DataOutputStream(mySock.getOutputStream());
        dis = new DataInputStream(mySock.getInputStream());
    }

	public String sendHttpRequest(String params)
	{		
		URL url;
		String result = "";
		try 
		{
			url = new URL(AUTHENTICATION_SERVER_ADDRESS);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			
			PrintWriter out = new PrintWriter(connection.getOutputStream());

           	out.println(params);
			out.close();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result = result.concat(inputLine);				
			}
			in.close();			
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}			
		
		if (result.length() == 0) {
			result = HTTP_REQUEST_FAILED;
		}
		
		return result;
		
	
	}

    @Override
    public byte[] sendHttpRequest(byte[] data) throws IOException {

        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(AUTHENTICATION_SERVER_ADDRESS);
        //httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        httpPost.setEntity(new ByteArrayEntity(data));

        //Log.i(TAG, "Write to server " + Helpers.byteArrayToHex(data));
        HttpResponse response = httpClient.execute(httpPost);


        HttpEntity entity2 = response.getEntity();
        byte[] bytes = Helpers.readBytes(entity2.getContent());

        //Log.i(TAG, "Read from server " + Helpers.byteArrayToHex(bytes));
        //String reconstitutedString = new String(bytes);

        //Log.i(TAG, "Read from server " + reconstitutedString);

        return bytes;
    }

    @Override
    public byte[] sendPacket(byte[] data) throws IOException {


        dos.write(data);
        byte[] tempdata = new byte[0];
        byte[] buffer = new byte[4096];
        int read = dis.read(buffer);
        if (read != -1) {
            tempdata = new byte[read];
            System.arraycopy(buffer, 0, tempdata, 0, read);
            Log.i(TAG, "doInBackground: Got some data " + Helpers.byteArrayToHex(tempdata));
        }else{
            Log.e(TAG, "read = -1 ");
        }
        return tempdata;


    }
}
