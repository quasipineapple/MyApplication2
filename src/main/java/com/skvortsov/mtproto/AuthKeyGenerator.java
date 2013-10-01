package com.skvortsov.mtproto;

import android.os.AsyncTask;
import android.util.Log;

import com.skvortsov.mtproto.communication.SocketOperator;
import com.skvortsov.mtproto.interfaces.ISocketOperator;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by сергей on 12.08.13.
 */
public class AuthKeyGenerator extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "AuthKeyGenerator";
    byte[] auth_key;
    byte[] server_nonce;
    byte[] new_nonce;
    byte[] auth_key_id;
    Boolean result;

    private byte[] salt;
    ISocketOperator socketOperator;
    Book book = BookManager.getBook();


    @Override
    protected Boolean doInBackground(Void... params) { //This runs on a different thread

        try {

            //InetAddress serverAddr = null;

            //serverAddr = InetAddress.getByName("95.142.192.65");
            //Socket mySock = new Socket(serverAddr, 80);
            //DataOutputStream dos = new DataOutputStream(mySock.getOutputStream());
            //DataInputStream dis = new DataInputStream(mySock.getInputStream());
            socketOperator = new SocketOperator();

            Log.i(TAG, "doInBackground: 2) Создаем req_pq.");
            Constructor req_pq = book.getConstructorByPredicate("req_pq").clone();
            req_pq.getParamByName("nonce").setData(Helpers.random_bytes(16));

            Log.i(TAG, "doInBackground: 3) Отправляем req_pq Получаем resPQ.");
            Constructor resPQ = MessageManager.parse(socketOperator.sendHttpRequest(
                    req_pq.toMessage().array())).toConstructor();
            //dos.write(req_pq.toMessage().toPacket().array());
            //Constructor resPQ = PacketManager.parse(read_answer(dis)).toMessage().toConstructor();

            Log.i(TAG, "doInBackground: 5) Раскладываем pq на 2 простых сомножителя.");
            BigInteger pq = new BigInteger((byte[]) resPQ.getParamByName("pq").getData());
            Log.i(TAG, "doInBackground: pq = " + pq.toString());
            PollardRho prho = new PollardRho();
            prho.factor(pq);
            BigInteger p = prho.getfactors2()[0];
            BigInteger q = prho.getfactors2()[1];
            prho.clear();
            if (p.compareTo(q) == 1) {Log.e(TAG, "doInBackground: ERROR p > q !!!!");}

            Log.i(TAG, "doInBackground: 6) Создаем p_q_inner_data.");
            Constructor p_q_inner_data = book.getConstructorByPredicate("p_q_inner_data").clone();
            p_q_inner_data.getParamByName("pq").setData(resPQ.getParamByName("pq").getData());
            p_q_inner_data.getParamByName("p").setData(p.toByteArray());
            p_q_inner_data.getParamByName("q").setData(q.toByteArray());
            p_q_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
            p_q_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
            p_q_inner_data.getParamByName("new_nonce").setData(Helpers.random_bytes(32));
            Log.w(TAG, "p_q_inner_data length = " + SerializationUtils.calcSize(p_q_inner_data));

            Log.i(TAG, "doInBackground: 7) Шифруем p_q_inner_data.");
            byte[] encrypted_p_q_inner_data = CryptoUtils.encrypt_p_q_inner_data(SerializationUtils.serialize(p_q_inner_data));
            Log.w(TAG, "encrypted_p_q_inner_data length = " + encrypted_p_q_inner_data.length);

            Log.i(TAG, "doInBackground: 8) Создаем req_DH_params.");
            Constructor req_DH_params = book.getConstructorByPredicate("req_DH_params").clone();
            req_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
            req_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
            req_DH_params.getParamByName("p").setData(p_q_inner_data.getParamByName("p").getData());
            req_DH_params.getParamByName("q").setData(p_q_inner_data.getParamByName("q").getData());
            Long[] server_public_key_fingerprints = (Long[]) resPQ.getParamByName("server_public_key_fingerprints").getData();
            req_DH_params.getParamByName("public_key_fingerprint").setData(server_public_key_fingerprints[0]);
            req_DH_params.getParamByName("encrypted_data").setData(encrypted_p_q_inner_data);

            Log.i(TAG, Helpers.byteArrayToHex(req_DH_params.toMessage().array()));

            Log.i(TAG, "doInBackground: 9) Отправляем req_DH_params. Получаем server_DH_params.");
            Constructor server_DH_params_ok = MessageManager.parse(socketOperator.sendHttpRequest(
                    req_DH_params.toMessage().array())).toConstructor();
            //dos.write(req_DH_params.toMessage().toPacket().array());
            //Constructor server_DH_params_ok = PacketManager.parse(read_answer(dis)).toMessage().toConstructor();
            if(!server_DH_params_ok.getPredicate().equals("server_DH_params_ok")){
                throw new RuntimeException("doInBackground: server_DH_params_ok id != " + server_DH_params_ok.getId());}

            Log.i(TAG, "doInBackground: 11) Расшифровываем encrypted_answer.");
            byte[] answer = CryptoUtils.decrypt_server_DH_params_ok(
                        (byte[]) server_DH_params_ok.getParamByName("encrypted_answer").getData(),
                        (byte[]) p_q_inner_data.getParamByName("new_nonce").getData(),
                        (byte[]) resPQ.getParamByName("server_nonce").getData());

            Log.i(TAG, "doInBackground: 12) Получаем server_DH_inner_data из answer.");
            Constructor server_DH_inner_data = SerializationUtils.deserialize(answer);

            Log.i(TAG, "doInBackground: 13) Создаем client_DH_inner_data.");
            Constructor client_DH_inner_data = book.getConstructorByPredicate("client_DH_inner_data").clone();
            client_DH_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
            client_DH_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
            client_DH_inner_data.getParamByName("retry_id").setData((long) 0);
            BigInteger bi_g = new BigInteger(server_DH_inner_data.getParamByName("g").getData().toString());
            BigInteger bi_b = new BigInteger(1, Helpers.random_bytes(256));
            BigInteger bi_dhPrime = new BigInteger(1, (byte[]) server_DH_inner_data.getParamByName("dh_prime").getData());
            BigInteger bi_g_b = bi_g.modPow(bi_b, bi_dhPrime);
            byte[] g_b = bi_g_b.toByteArray();
            client_DH_inner_data.getParamByName("g_b").setData(g_b);
            byte[] serialized_client_DH_inner_data = SerializationUtils.serialize(client_DH_inner_data);

            Log.i(TAG, "doInBackground: 14) Шифруем client_DH_inner_data.");
            byte[] encrypted_client_DH_inner_data = CryptoUtils.encrypt_client_DH_inner_data(
                        serialized_client_DH_inner_data,
                        (byte[]) p_q_inner_data.getParamByName("new_nonce").getData(),
                        (byte[]) resPQ.getParamByName("server_nonce").getData());

            Log.w(TAG, "encrypted_client_DH_inner_data length = " + encrypted_client_DH_inner_data.length);

            Log.i(TAG, "doInBackground: 15) Создаем set_client_DH_params.");
            Constructor set_client_DH_params = book.getConstructorByPredicate("set_client_DH_params").clone();
            set_client_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
            set_client_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
            set_client_DH_params.getParamByName("encrypted_data").setData(encrypted_client_DH_inner_data);

            Log.i(TAG, "doInBackground: 16) Отправляем set_client_DH_params. Получаем dh_gen_ok.");
            Log.i(TAG, Helpers.byteArrayToHex(set_client_DH_params.toMessage().array()));
            Constructor dh_gen = MessageManager.parse(socketOperator.sendHttpRequest(
                set_client_DH_params.toMessage().array())).toConstructor();
            if(!dh_gen.getPredicate().equals("dh_gen_ok")){
                throw new RuntimeException("doInBackground: dh_gen id != " + dh_gen.getId());}

            Log.i(TAG, "doInBackground: 18) Вычисляем auth_key.");
            BigInteger bi_g_a = new BigInteger(1, (byte[]) server_DH_inner_data.getParamByName("g_a").getData());
            BigInteger bi_auth_key = bi_g_a.modPow(bi_b, bi_dhPrime);
            byte[] auth_key = bi_auth_key.toByteArray();
            if (auth_key[0] == 0) {
                Log.w(TAG, "doInBackground: auth_key[0] == 0");
                byte[] tmp = new byte[auth_key.length - 1];
                System.arraycopy(auth_key, 1, tmp, 0, tmp.length);
                auth_key = tmp;
            }

            Log.i(TAG, "doInBackground: auth_key: " +  Helpers.byteArrayToHex(auth_key));
            this.auth_key = auth_key;
            this.server_nonce = (byte[]) resPQ.getParamByName("server_nonce").getData();
            this.new_nonce = (byte[]) p_q_inner_data.getParamByName("new_nonce").getData();

            // auth_key_id
            byte[] inner_auth_key_id = new byte[0];
            try {
                inner_auth_key_id = new BigInteger(1, Helpers.substr(Helpers.SHA1(this.auth_key), 12, 20)).toByteArray();
                //inner_auth_key_id = Arrays.copyOfRange(Helpers.SHA1(this.auth_key), 12, 20);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if (inner_auth_key_id[0] == 0) {
                byte[] tmp = new byte[inner_auth_key_id.length - 1];
                System.arraycopy(inner_auth_key_id, 1, tmp, 0, tmp.length);
                inner_auth_key_id = tmp;
            }
            this.auth_key_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(inner_auth_key_id).array();
            //this.auth_key_id = inner_auth_key_id;

            // salt
            byte[] array_1 = Arrays.copyOfRange(server_nonce, 0, 8);
            byte[] array_2 = Arrays.copyOfRange(new_nonce, 0, 8);
            byte[] bb = new byte[8];
            int i = 0;
            for (byte b : array_1)
                bb[i] = (byte) (b ^ array_2[i++]);
            this.salt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(bb).array();
            //this.salt = bb;

        } catch (Exception e) {
            e.printStackTrace();

            Log.i(TAG, "doInBackground: Exception");
            result = false;
        } finally {
            result = true;
            Log.i(TAG, "doInBackground: Finished");
        }
        return result;
    }

    private byte[] read_answer(DataInputStream dis) throws IOException {
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

    @Override
    protected void onCancelled() {
        Log.i(TAG, "Cancelled.");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            Log.i(TAG, "onPostExecute: Completed with an Error.");
        } else {
            SessionManager.getS().setAuth_key(auth_key);
            SessionManager.getS().setNew_nonce(new_nonce);
            SessionManager.getS().setServer_nonce(server_nonce);
            SessionManager.getS().setAuth_key_id(auth_key_id);
            SessionManager.getS().setSession_id(Helpers.random_bytes(8));
            SessionManager.getS().setSalt(salt);
            SessionManager.saveSession();


            Log.i(TAG, "onPostExecute: Completed.");
        }
    }
}
