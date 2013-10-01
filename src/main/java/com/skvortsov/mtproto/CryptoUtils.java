package com.skvortsov.mtproto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

/**
 * Created by skvortsov on 08.08.13.
 */
public class CryptoUtils {

    public static byte[] encrypt_p_q_inner_data(byte[] p_q_inner_data) throws NoSuchAlgorithmException {

        //!!!!!!!!
        ByteBuffer bb_data_with_hash = ByteBuffer.allocate(255);
        byte[] sha1 = Helpers.SHA1(p_q_inner_data);
        bb_data_with_hash.put(sha1);
        bb_data_with_hash.put(p_q_inner_data);
        byte[] data_with_hash = bb_data_with_hash.array();


        BigInteger e = new BigInteger("010001", 16);
        BigInteger r = new BigInteger(1, data_with_hash);
        BigInteger m = new BigInteger("C150023E2F70DB7985DED064759CFECF0AF328E69A41DAF4D6F01B538135A6F91F8F8B2A0EC9BA9720CE352EFCF6C5680FFC424BD634864902DE0B4BD6D49F4E580230E3AE97D95C8B19442B3C0A10D8F5633FECEDD6926A7F6DAB0DDB7D457F9EA81B8465FCD6FFFEED114011DF91C059CAEDAF97625F6C96ECC74725556934EF781D866B34F011FCE4D835A090196E9A5F0E4449AF7EB697DDB9076494CA5F81104A305B6DD27665722C46B60E5DF680FB16B210607EF217652E60236C255F6A28315F4083A96791D7214BF64C1DF4FD0DB1944FB26A2A57031B32EEE64AD15A8BA68885CDE74A5BFC920F6ABF59BA5C75506373E7130F9042DA922179251F",16);
        BigInteger s = r.modPow(e, m);
        byte[] array = s.toByteArray();

        if (array[0] == 0) {
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }

        return array;
    }

    public static byte[] decrypt_server_DH_params_ok(byte[] encrypted_data, byte[] new_nonce, byte[] server_nonce) throws NoSuchAlgorithmException {

        byte[] new_nonce_plus_server_nonce = Helpers.concat(new_nonce, server_nonce);
        byte[] server_nonce_plus_new_nonce = Helpers.concat(server_nonce, new_nonce);
        byte[] new_nonce_plus_new_nonce = Helpers.concat(new_nonce, new_nonce);

        byte[] sha_new_nonce_plus_server_nonce = Helpers.SHA1(new_nonce_plus_server_nonce);
        byte[] sha_server_nonce_plus_new_nonce = Helpers.SHA1(server_nonce_plus_new_nonce);
        byte[] sha_new_nonce_plus_new_nonce = Helpers.SHA1(new_nonce_plus_new_nonce);

        byte[] key = Helpers.concat(sha_new_nonce_plus_server_nonce, Helpers.substr(sha_server_nonce_plus_new_nonce, 0, 12));
        byte[] iv = Helpers.concat(Helpers.concat(Helpers.substr(sha_server_nonce_plus_new_nonce,12,20),sha_new_nonce_plus_new_nonce),Helpers.substr(new_nonce, 0, 4));
        byte[] iv_1 = Helpers.substr(iv, 0, 16);
        byte[] iv_2 = Helpers.substr(iv, 16, 32);

        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);
        byte[] answer_with_hash = aes_ige.decrypt(encrypted_data);

        return Helpers.substr(answer_with_hash, 20, answer_with_hash.length);
    }

    public static byte[] encrypt_client_DH_inner_data(byte[] data, byte[] new_nonce, byte[] server_nonce) throws NoSuchAlgorithmException {

        byte[] new_nonce_plus_server_nonce = Helpers.concat(new_nonce, server_nonce);
        byte[] server_nonce_plus_new_nonce = Helpers.concat(server_nonce, new_nonce);
        byte[] new_nonce_plus_new_nonce = Helpers.concat(new_nonce, new_nonce);

        byte[] sha_new_nonce_plus_server_nonce = Helpers.SHA1(new_nonce_plus_server_nonce);
        byte[] sha_server_nonce_plus_new_nonce = Helpers.SHA1(server_nonce_plus_new_nonce);
        byte[] sha_new_nonce_plus_new_nonce = Helpers.SHA1(new_nonce_plus_new_nonce);

        byte[] key = Helpers.concat(sha_new_nonce_plus_server_nonce, Helpers.substr(sha_server_nonce_plus_new_nonce, 0, 12));
        byte[] iv = Helpers.concat(Helpers.concat(Helpers.substr(sha_server_nonce_plus_new_nonce,12,20),sha_new_nonce_plus_new_nonce),Helpers.substr(new_nonce, 0, 4));
        byte[] iv_1 = Helpers.substr(iv, 0, 16);
        byte[] iv_2 = Helpers.substr(iv, 16, 32);

        byte[] sha1 = Helpers.SHA1(data);
        byte[] data_with_hash = Helpers.concat(sha1, data);

        int i = data_with_hash.length;

        while(i % 16 != 0){i++;}
        /* public static native void arraycopy(Object src, int srcPos, Object dst, int dstPos, int length);*/
        if(i!=data_with_hash.length){
            byte[] tmp = new byte[i];
            System.arraycopy(data_with_hash, 0, tmp, 0, data_with_hash.length);
            byte[] rnd = Helpers.random_bytes(i - data_with_hash.length);
            System.arraycopy(rnd, 0, tmp, data_with_hash.length,  rnd.length);
            data_with_hash = tmp;
        }

        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);
        return aes_ige.encrypt(data_with_hash);
    }


    public static byte[] encrypt_data(Data data, byte[] auth_key, byte[] msg_key) throws NoSuchAlgorithmException {

        int x = 0;

        byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32)));
        byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8));

        byte[] w_array = data.array();

        int i = w_array.length;

        while(i % 16 != 0){i++;}

        if(i!=w_array.length){
            byte[] tmp = new byte[i];
            System.arraycopy(w_array, 0, tmp, 0, w_array.length);
            byte[] rnd = Helpers.random_bytes(i - w_array.length);
            System.arraycopy(rnd, 0, tmp, w_array.length, rnd.length);
            w_array = tmp;
        }

        byte[] iv_1 = Helpers.substr(aes_iv, 0, 16);
        byte[] iv_2 = Helpers.substr(aes_iv, 16, 32);
        AES_IGE aes_ige = new AES_IGE(aes_key, iv_1, iv_2);

        return aes_ige.encrypt(w_array);

    }

    public static byte[] encrypt_data(byte[] data, byte[] auth_key, byte[] msg_key) throws NoSuchAlgorithmException {

        int x = 0;

        byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32)));
        byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8));



        if(data.length % 16 != 0)
        {
            System.out.println("WARNING data.length % 16 != 0 !!!!");
            int i = data.length + 1;

            while(i % 16 != 0){
                i = i + 1;
            }

            System.out.println("data length " + data.length + " increase to " + i);
            byte[] tmp = new byte[i];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
            System.out.println("new data length " + data.length);
        }


        System.out.println("data " + Helpers.bytesToHex(data));
        byte[] iv_1 = Helpers.substr(aes_iv, 0, 16);
        byte[] iv_2 = Helpers.substr(aes_iv, 16, 32);

        AES_IGE aes_ige = new AES_IGE(aes_key, iv_1, iv_2);

        return aes_ige.encrypt(data);

    }

    public static byte[] decrypt_data( byte[] data, byte[] auth_key, byte[] msg_key) throws NoSuchAlgorithmException {


        int x = 8;

        byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32 + x)));

        byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8));

        byte[] iv_1 = Helpers.substr(aes_iv, 0, 16);
        byte[] iv_2 = Helpers.substr(aes_iv, 16, 32);

        AES_IGE aes_ige = new AES_IGE(aes_key, iv_1, iv_2);

        return aes_ige.decrypt(data);


    }
}
