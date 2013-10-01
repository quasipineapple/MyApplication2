package com.skvortsov.mtproto;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;

/**
 * Created by сергей on 09.08.13.
 */
public class MyActivity extends Activity{

    Button btnStart, btnSend;
    TextView textStatus;
    byte[] auth_key = new byte[0];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main3);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnSend = (Button)findViewById(R.id.btnSend);
        textStatus = (TextView)findViewById(R.id.textStatus);
        btnStart.setOnClickListener(btnStartListener);
        btnSend.setOnClickListener(btnSendListener);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String s_auth_key = sharedPref.getString("auth_key", "00");
        auth_key = Helpers.hexStringToByteArray(s_auth_key);

        if(auth_key.length == 256){
            Log.i("MyActivity", "auth_key loaded from preferences.");
        }else{
            Log.i("MyActivity","Generating new Auth key...");
            new NetworkTask().execute();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        Log.i("MyActivity", "onSaveInstanceState!");
        savedInstanceState.putByteArray("auth_key", auth_key);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        Log.i("MyActivity", "onRestoreInstanceState!");
        auth_key = savedInstanceState.getByteArray("auth_key");
    }

    private View.OnClickListener btnStartListener = new View.OnClickListener() {
        public void onClick(View v){
        }
    };
    private View.OnClickListener btnSendListener = new View.OnClickListener() {
        public void onClick(View v){



        }
    };

    public class NetworkTask extends AsyncTask<Void, Void, byte[]> {
        Socket nsocket; //Network Socket
        DataInputStream nis; //Network Input Stream
        DataOutputStream nos; //Network Output Stream
        byte[] result = new byte[0];

        @Override
        protected byte[] doInBackground(Void... params) { //This runs on a different thread
            try {
                Log.i("AsyncTask", "doInBackground: Creating socket");
                SocketAddress sockaddr = new InetSocketAddress("95.142.192.65", 80);
                nsocket = new Socket();
                nsocket.connect(sockaddr, 5000); //10 second connection timeout

                if (nsocket.isConnected()) {
                    nis = new DataInputStream(nsocket.getInputStream());
                    nos = new DataOutputStream(nsocket.getOutputStream());
                    Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");

                    Log.i("AsyncTask", "doInBackground: 1) Загружаем объекты из json.");
                    InputStream source = getResources().openRawResource(R.raw.json);
                    InputStream source2 = getResources().openRawResource(R.raw.json2);
                    Gson gson = new Gson();
                    Reader reader = new InputStreamReader(source);
                    Reader reader2 = new InputStreamReader(source2);
                    Book book = gson.fromJson(reader, Book.class);
                    Book book2 = gson.fromJson(reader2, Book.class);
                    SerializationUtils.setBook2(book2);

                    Log.i("AsyncTask", "doInBackground: 2) Создаем req_pq.");
                    Constructor req_pq = book2.getConstructorByPredicate("req_pq").clone();
                    byte[] b2 = new byte[16];
                    new Random().nextBytes(b2);
                    req_pq.getParamByName("nonce").setData(b2);

                    Log.i("AsyncTask", "doInBackground: 3) Отправляем req_pq.");
                    Message m_req_pq = new Message(SerializationUtils.serialize(req_pq));
                    Packet p_req_pq = new Packet(m_req_pq, 0);
                    nos.write(p_req_pq.array());
                    Packet p_resPQ = new Packet(read_answer(nis));

                    Log.i("AsyncTask", "doInBackground: 4) Получаем resPQ.");
                    Constructor resPQ = SerializationUtils.deserialize(p_resPQ.getM().getMessage_data());

                    Log.i("AsyncTask", "doInBackground: 5) Раскладываем pq на 2 простых сомножителя.");
                    BigInteger pq = new BigInteger((byte[]) resPQ.getParamByName("pq").getData());
                    Log.i("AsyncTask", "doInBackground: pq = " + pq.toString());
                    PollardRho prho = new PollardRho();
                    prho.factor(pq);
                    BigInteger p = prho.getfactors2()[0];
                    BigInteger q = prho.getfactors2()[1];
                    prho.clear();
                    if (p.compareTo(q) == 1) {Log.e("AsyncTask", "doInBackground: ERROR p > q !!!!");}

                    Log.i("AsyncTask", "doInBackground: 6) Создаем p_q_inner_data.");
                    Constructor p_q_inner_data = book2.getConstructorByPredicate("p_q_inner_data").clone();
                    p_q_inner_data.getParamByName("pq").setData(resPQ.getParamByName("pq").getData());
                    p_q_inner_data.getParamByName("p").setData(p.toByteArray());
                    p_q_inner_data.getParamByName("q").setData(q.toByteArray());
                    p_q_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
                    p_q_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());;
                    p_q_inner_data.getParamByName("new_nonce").setData(random_bytes(32));

                    Log.i("AsyncTask", "doInBackground: 7) Шифруем p_q_inner_data.");
                    byte[] encrypted_p_q_inner_data = CryptoUtils.encrypt_p_q_inner_data(SerializationUtils.serialize(p_q_inner_data));

                    Log.i("AsyncTask", "doInBackground: 8) Создаем req_DH_params.");
                    Constructor req_DH_params = book2.getConstructorByPredicate("req_DH_params").clone();
                    req_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
                    req_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
                    req_DH_params.getParamByName("p").setData(p_q_inner_data.getParamByName("p").getData());
                    req_DH_params.getParamByName("q").setData(p_q_inner_data.getParamByName("q").getData());
                    Long[] server_public_key_fingerprints = (Long[]) resPQ.getParamByName("server_public_key_fingerprints").getData();
                    req_DH_params.getParamByName("public_key_fingerprint").setData(server_public_key_fingerprints[0]);
                    req_DH_params.getParamByName("encrypted_data").setData(encrypted_p_q_inner_data);

                    Log.i("AsyncTask", "doInBackground: 9) Отправляем req_DH_params.");
                    Message m_req_DH_params = new Message(SerializationUtils.serialize(req_DH_params));
                    Packet p_req_DH_params = new Packet(m_req_DH_params, 1);
                    nos.write(p_req_DH_params.array());
                    Packet p_server_DH_params = new Packet(read_answer(nis));

                    Log.i("AsyncTask", "doInBackground: 10) Получаем server_DH_params.");
                    Constructor server_DH_params_ok = null;
                    server_DH_params_ok = SerializationUtils.deserialize(p_server_DH_params.getM().getMessage_data());

                    Log.i("AsyncTask", "doInBackground: 11) Расшифровываем encrypted_answer.");
                    byte[] answer = CryptoUtils.decrypt_server_DH_params_ok(
                            (byte[]) server_DH_params_ok.getParamByName("encrypted_answer").getData(),
                            (byte[]) p_q_inner_data.getParamByName("new_nonce").getData(),
                            (byte[]) resPQ.getParamByName("server_nonce").getData());

                    Log.i("AsyncTask", "doInBackground: 12) Получаем server_DH_inner_data из answer.");
                    Constructor server_DH_inner_data = SerializationUtils.deserialize(answer);

                    Log.i("AsyncTask", "doInBackground: 13) Создаем client_DH_inner_data.");
                    Constructor client_DH_inner_data = book2.getConstructorByPredicate("client_DH_inner_data").clone();
                    client_DH_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
                    client_DH_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
                    client_DH_inner_data.getParamByName("retry_id").setData((long) 0);
                    BigInteger bi_g = new BigInteger(server_DH_inner_data.getParamByName("g").getData().toString());
                    BigInteger bi_b = new BigInteger(1, random_bytes(256));
                    BigInteger bi_dhPrime = new BigInteger(1, (byte[]) server_DH_inner_data.getParamByName("dh_prime").getData());
                    BigInteger bi_g_b = bi_g.modPow(bi_b, bi_dhPrime);
                    byte[] g_b = bi_g_b.toByteArray();
                    client_DH_inner_data.getParamByName("g_b").setData(g_b);
                    byte[] serialized_client_DH_inner_data = SerializationUtils.serialize(client_DH_inner_data);

                    Log.i("AsyncTask", "doInBackground: 14) Шифруем client_DH_inner_data.");
                    byte[] encrypted_client_DH_inner_data = CryptoUtils.encrypt_client_DH_inner_data(
                            serialized_client_DH_inner_data,
                            (byte[]) p_q_inner_data.getParamByName("new_nonce").getData(),
                            (byte[]) resPQ.getParamByName("server_nonce").getData());

                    Log.i("AsyncTask", "doInBackground: 15) Создаем set_client_DH_params.");
                    Constructor set_client_DH_params = book2.getConstructorByPredicate("set_client_DH_params").clone();
                    set_client_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
                    set_client_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
                    set_client_DH_params.getParamByName("encrypted_data").setData(encrypted_client_DH_inner_data);

                    Log.i("AsyncTask", "doInBackground: 16) Отправляем req_DH_params.");
                    Message m_set_client_DH_params = new Message(SerializationUtils.serialize(set_client_DH_params));
                    Packet p_set_client_DH_params = new Packet(m_set_client_DH_params, 2);
                    nos.write(p_set_client_DH_params.array());
                    Packet p_dh_gen = new Packet(read_answer(nis));

                    Log.i("AsyncTask", "doInBackground: 17) Получаем dh_gen_ok.");
                    Constructor dh_gen = SerializationUtils.deserialize(p_dh_gen.getM().getMessage_data());
                    if(!dh_gen.getId().equals("1003222836")){Log.e("AsyncTask", "doInBackground: dh_gen must be 1003222836, but is = " + dh_gen.getId());}

                    Log.i("AsyncTask", "doInBackground: 18) Вычисляем auth_key.");
                    BigInteger bi_g_a = new BigInteger(1, (byte[]) server_DH_inner_data.getParamByName("g_a").getData());
                    BigInteger bi_auth_key = bi_g_a.modPow(bi_b, bi_dhPrime);
                    byte[] auth_key = bi_auth_key.toByteArray();
                    if (auth_key[0] == 0) {
                        Log.w("AsyncTask", "doInBackground: auth_key[0] == 0");
                        byte[] tmp = new byte[auth_key.length - 1];
                        System.arraycopy(auth_key, 1, tmp, 0, tmp.length);
                        auth_key = tmp;
                    }
                    Log.i("AsyncTask", "doInBackground: auth_key: " +  Helpers.byteArrayToHex(auth_key));
                    result = auth_key;

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: IOException");
                //result = 0x00;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("AsyncTask", "doInBackground: Exception");
                //result = true;
            } finally {
                try {
                    nis.close();
                    nos.close();
                    nsocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("AsyncTask", "doInBackground: Finished");
            }
            return result;
        }

        private byte[] random_bytes(int i){
            byte[] b = new byte[i];
            new Random().nextBytes(b);
            return b;
        }

        private byte[] read_answer(DataInputStream nis) throws IOException {
            byte[] tempdata = new byte[0];
            byte[] buffer = new byte[4096];
            int read = nis.read(buffer);
            if (read != -1) {
                tempdata = new byte[read];
                System.arraycopy(buffer, 0, tempdata, 0, read);
                Log.i("AsyncTask", "doInBackground: Got some data " + Helpers.byteArrayToHex(tempdata));
            }
            return tempdata;
        }

        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }

        @Override
        protected void onPostExecute(byte[] result) {
            if (result.length == 0) {
                Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
            } else {
                auth_key = result;
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("auth_key", Helpers.bytesToHex(auth_key));
                editor.commit();
                Log.i("AsyncTask", "onPostExecute: Completed.");
            }
        }
    }


}

