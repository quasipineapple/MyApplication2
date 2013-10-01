package com.skvortsov.mtproto;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by skvortsov on 01.08.13.
 */
public class qwe {
    public static void main(String[] args) throws CloneNotSupportedException, IOException, NoSuchAlgorithmException {

        //String url = "http://dev.stel.com/scheme/json";


        //InputStream is = getResources().openRawResource(R.raw.json_file);

        InputStream source = null, source2 = null;
        try {
            //source = retrieveStream(url);
            source = new FileInputStream("C:\\1\\json.js");
            source2 = new FileInputStream("C:\\1\\json2.js");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Reader reader = new InputStreamReader(source);
        Book book = gson.fromJson(reader, Book.class);
        Reader reader2  = new InputStreamReader(source2);
        Book book2 = gson.fromJson(reader2, Book.class);

        Transport sender = new Transport("95.142.192.65", 80);
        sender.connect();

        SerializationUtils.setBook2(book2);

        // создаем req_pq
        Constructor req_pq = (Constructor) book2.getConstructorByPredicate("req_pq").clone();
        byte[] b2 = new byte[16];
        new Random().nextBytes(b2);
        req_pq.getParamByName("nonce").setData(b2);

        // отправляем req_pq
        Message m_req_pq = new Message(SerializationUtils.serialize(req_pq));
        Packet p_req_pq = new Packet(m_req_pq, 0);
        Packet p_resPQ = sender.send(p_req_pq);

        // получаем resPQ
        Constructor resPQ = SerializationUtils.deserialize(p_resPQ.getM().getMessage_data());

        // раскладываем pq на 2 простых сомножителя
        BigInteger pq = new BigInteger((byte[])resPQ.getParamByName("pq").getData());
        System.out.println("pq = " + pq.toString());

        PollardRho prho = new PollardRho();
        prho.factor(pq);
        BigInteger p = prho.getfactors2()[0];
        BigInteger q = prho.getfactors2()[1];
        prho.clear();

        if (p.compareTo(q) == 1){
            throw new RuntimeException("ERROR p > q !!!!");
        }

        // создаем p_q_inner_data
        Constructor p_q_inner_data = (Constructor)book2.getConstructorByPredicate("p_q_inner_data").clone();
        p_q_inner_data.getParamByName("pq").setData(resPQ.getParamByName("pq").getData());
        p_q_inner_data.getParamByName("p").setData(p.toByteArray());
        p_q_inner_data.getParamByName("q").setData(q.toByteArray());
        p_q_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
        p_q_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        p_q_inner_data.getParamByName("new_nonce").setData(b);
        byte[] data333 = SerializationUtils.serialize(p_q_inner_data);

        // шифруем p_q_inner_data
        byte[] encrypted_p_q_inner_data = CryptoUtils.encrypt_p_q_inner_data(data333);

        // создаем req_DH_params
        Constructor req_DH_params = (Constructor)book2.getConstructorByPredicate("req_DH_params").clone();
        req_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
        req_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
        req_DH_params.getParamByName("p").setData(p_q_inner_data.getParamByName("p").getData());
        req_DH_params.getParamByName("q").setData(p_q_inner_data.getParamByName("q").getData());
        Long[] server_public_key_fingerprints = (Long[]) resPQ.getParamByName("server_public_key_fingerprints").getData();
        req_DH_params.getParamByName("public_key_fingerprint").setData(server_public_key_fingerprints[0]);
        req_DH_params.getParamByName("encrypted_data").setData(encrypted_p_q_inner_data);


        // отправляем req_DH_params
        Message m_req_DH_params = new Message(SerializationUtils.serialize(req_DH_params));
        //System.out.println(Helpers.byteArrayToHex(m_req_DH_params.array()));
        Packet p_req_DH_params = new Packet(m_req_DH_params, 1);
        Packet p_server_DH_params = sender.send(p_req_DH_params);

        // получаем server_DH_params
        Constructor server_DH_params_ok = SerializationUtils.deserialize(p_server_DH_params.getM().getMessage_data());

        // расшифровываем encrypted_answer
        byte[] answer = CryptoUtils.decrypt_server_DH_params_ok(
                (byte[])server_DH_params_ok.getParamByName("encrypted_answer").getData(),
                (byte[])p_q_inner_data.getParamByName("new_nonce").getData(),
                (byte[])resPQ.getParamByName("server_nonce").getData());

        // получаем server_DH_inner_data из answer
        Constructor server_DH_inner_data = SerializationUtils.deserialize(answer);
        System.out.println(server_DH_inner_data.getPredicate());


        // создаем client_DH_inner_data
        Constructor client_DH_inner_data = (Constructor)book2.getConstructorByPredicate("client_DH_inner_data").clone();
        client_DH_inner_data.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
        client_DH_inner_data.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
        client_DH_inner_data.getParamByName("retry_id").setData((long)0);
        byte[] bb = new byte[256];
        new Random().nextBytes(bb);
        BigInteger bi_g = new BigInteger(server_DH_inner_data.getParamByName("g").getData().toString());
        BigInteger bi_b = new BigInteger(1, bb);
        BigInteger bi_dhPrime = new BigInteger(1, (byte[])server_DH_inner_data.getParamByName("dh_prime").getData());
        BigInteger bi_g_b = bi_g.modPow(bi_b, bi_dhPrime);
        byte[] g_b = bi_g_b.toByteArray();
        client_DH_inner_data.getParamByName("g_b").setData(g_b);
        byte[] serialized_client_DH_inner_data = SerializationUtils.serialize(client_DH_inner_data);


        // шифруем client_DH_inner_data
        byte[] encrypted_client_DH_inner_data = CryptoUtils.encrypt_client_DH_inner_data(
                serialized_client_DH_inner_data,
                (byte[])p_q_inner_data.getParamByName("new_nonce").getData(),
                (byte[])resPQ.getParamByName("server_nonce").getData());

        // создаем set_client_DH_params
        Constructor set_client_DH_params = (Constructor)book2.getConstructorByPredicate("set_client_DH_params").clone();
        set_client_DH_params.getParamByName("nonce").setData(resPQ.getParamByName("nonce").getData());
        set_client_DH_params.getParamByName("server_nonce").setData(resPQ.getParamByName("server_nonce").getData());
        set_client_DH_params.getParamByName("encrypted_data").setData(encrypted_client_DH_inner_data);

        // отправляем req_DH_params
        Message m_set_client_DH_params = new Message(SerializationUtils.serialize(set_client_DH_params));
        Packet p_set_client_DH_params = new Packet(m_set_client_DH_params, 2);
        Packet p_dh_gen = sender.send(p_set_client_DH_params);

        // получаем dh_gen_ok или dh_gen_retry или dh_gen_fail
        Constructor dh_gen  = SerializationUtils.deserialize(p_dh_gen.getM().getMessage_data());
        System.out.println("dh_gen must be 1003222836 = " + dh_gen.getId());


        sender.disconnect();
        //book2.print();
        //book.print();

    }


    private static InputStream retrieveStream(String url) throws IOException {

        DefaultHttpClient client = new DefaultHttpClient();

        HttpGet getRequest = new HttpGet(url);

        try {

            HttpResponse getResponse = client.execute(getRequest);
            final int statusCode = getResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                //Log.w(getClass().getSimpleName(),
                //        "Error " + statusCode + " for URL " + url);
                return null;
            }

            HttpEntity getResponseEntity = getResponse.getEntity();
            return getResponseEntity.getContent();

        }
        catch (IOException e) {
            getRequest.abort();
            //Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
        }

        return null;

    }
}
