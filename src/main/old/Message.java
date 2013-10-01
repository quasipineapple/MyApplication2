package com.tl;

import com.skvortsov.mtproto.Transport;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 20.07.13
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class Message {


    public static void main(String args[]) throws NoSuchAlgorithmException, IOException {

        Transport sender = new Transport("95.142.192.65", 80);
        sender.connect();

        String auth_key_str = "1EFAC7A73B3507B8C642ACA080F7CD86450ECF5346419125FA48C19257B6B043E093DADE271BF94D198044CC1342926E0EAF4B2359F68888E559FFE8E3518883FB04F7D5469D125C83864D23F35120C11F4AAE7E67A7FF2792EC2B8B883ED9091B9B9853A79900C3260C904579516D517C77F1077D33571F2BAD251C99CF1EE06CE264B531E1532D41DA74BB7FDC376343D99E58BFD9E4C560E5CF7062B02425B29742BB1C3FE6B9B0A16D3F0039BFCF71EEE5176BD039C4F9B1BE3C6D58FB01A64A721FC0002568273FBCFD73861D89928E8C6202998A16312D4FB368B53034D9AAD8D78E75070DBAF8C37EB9EF0D7B5AA009DBDCF56FBE08E06C170BE52CEA";
        byte[] auth_key = Helpers.hexStringToByteArray(auth_key_str);


        byte[] sha_from_auth_key = Helpers.SHA1(auth_key);
        System.out.println("sha_from_auth_key: " + Helpers.bytesToHex(sha_from_auth_key));
        byte[] auth_key_id = Helpers.substr(sha_from_auth_key, 12, 20);

        BigInteger bi_auth_key_id = new BigInteger(1, auth_key_id);
        System.out.println("bi_auth_key_id: " + bi_auth_key_id.toString());
        byte[] auth_key_id2 = bi_auth_key_id.toByteArray();

        System.out.println(auth_key_id2.length);

        if (auth_key_id2[0] == 0) {

            System.out.println("WARNING auth_key_id2.length == 9 !!!!");
            byte[] tmp = new byte[auth_key_id2.length - 1];
            System.arraycopy(auth_key_id2, 1, tmp, 0, tmp.length);
            auth_key_id2 = tmp;
        }

        auth_key_id2 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(auth_key_id2).array();
        System.out.println("auth_key_id2: " + Helpers.bytesToHex(auth_key_id2));

        //salt LE
        //byte[] array_1 = Arrays.copyOfRange(server_nonce, 0, 8);
        //byte[] array_2 = Arrays.copyOfRange(new_nonce, 0, 8);

        //byte[] salt = new byte[8];

        //int i = 0;
        //for (byte b : array_1)
        //    salt[i] = (byte) (b ^ array_2[i++]);


        byte[] salt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(0).array();
        //salt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(salt).array();
        System.out.println("salt: " + Helpers.bytesToHex(salt));

        //1) create_data_for_ping
        byte[] data_for_ping = create_data_for_ping(salt);
        System.out.println("data_for_ping: " + Helpers.bytesToHex(data_for_ping));

        //2) msgKey
        byte[] msgKey = Helpers.substr(Helpers.SHA1(data_for_ping), 4, 20);
        BigInteger bi_msgKey = new BigInteger(1, msgKey);
        System.out.println("bi_msgKey: " + bi_msgKey.toString());
        byte[] msgKey2 = bi_msgKey.toByteArray();
        System.out.println("msgKey length: " + msgKey2.length);

        if (msgKey2[0] == 0) {

            System.out.println("WARNING msgKey2 length == 9 !!!!");
            byte[] tmp = new byte[msgKey2.length - 1];
            System.arraycopy(msgKey2, 1, tmp, 0, tmp.length);
            msgKey2 = tmp;
        }

        msgKey2 = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).put(msgKey2).array();
        System.out.println("msgKey2: " + Helpers.bytesToHex(msgKey2));



        //3) encrypt_data
        byte[] encrypted_data_with_ping = encrypt_data(data_for_ping, auth_key, msgKey2);
        System.out.println("encrypted_data_with_ping: " + Helpers.bytesToHex(encrypted_data_with_ping));

        //4) result
        byte[] result;
        result = Helpers.concat(Helpers.concat(auth_key_id2, msgKey2), encrypted_data_with_ping);
        System.out.println("auth_key_id + msgKey + encrypted_data: " + Helpers.bytesToHex(result));
        System.out.println("auth_key_id + msgKey + encrypted_data length: " + result.length);


        //длина
        byte[] length_for_tcp;
        length_for_tcp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4 + 4 + result.length + 4).array();
        System.out.println("length_for_tcp: " + Helpers.bytesToHex(length_for_tcp));

        //порядковый номер пакета - 3
        byte[] packet_numb_for_tcp;
        packet_numb_for_tcp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        System.out.println("packet_numb_for_tcp: " + Helpers.bytesToHex(packet_numb_for_tcp));

        //собираем пакет
        byte[] result2;
        result2 = Helpers.concat(Helpers.concat(length_for_tcp, packet_numb_for_tcp), result);
        System.out.println("result2: " + Helpers.bytesToHex(result2));


        //CRC32
        Checksum checksum = new CRC32();
        checksum.update(result2, 0, result2.length);
        byte[] crc32 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) checksum.getValue()).array();
        System.out.println("CRC32: " + Helpers.bytesToHex(crc32));

        //собираем пакет
        byte[] result3;
        result3 = Helpers.concat(result2, crc32);
        System.out.println("result3: " + Helpers.bytesToHex(result3));
        System.out.println("result3 length: " + result3.length);


        byte[] answer;
        answer = sender.send(result3);
        System.out.println("answer: " + Helpers.bytesToHex(answer));


       sender.disconnect();



    }

    private static byte[] create_data_for_ping(byte[] salt) {


        byte[] data;

        data = salt;
        //salt
        //data = Helpers.concat(data, salt);

        //session_id
        byte[] session_id = new byte[8];
        new Random().nextBytes(session_id);
        session_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(session_id).array();
        //session_id = Helpers.hexStringToByteArray("1111111122222222");
        //session_id = ByteBuffer.allocate(8).put(session_id).array();
        System.out.println("session_id: " + Helpers.bytesToHex(session_id));
        data = Helpers.concat(data, session_id);

        //message_id
        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        //long unixTime = ((System.currentTimeMillis() / 1000L) * 2) ^ 32;
        if(unixTime % 4 != 0)
        {
            System.out.println("message_id % 4 != 0 !!!!");
            unixTime = unixTime + 1;

            while(unixTime % 4 != 0){
                unixTime = unixTime + 1;
            }

            System.out.println("message_id increase to " + unixTime);

        }

        //byte[] mes_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array();
        byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
        System.out.println("message_id: " + Helpers.bytesToHex(mes_id));


        data = Helpers.concat(data, mes_id);

        //msg_seqno 4
        byte[] msg_seqno = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
        System.out.println("msg_seqno: " + Helpers.bytesToHex(msg_seqno));
        data = Helpers.concat(data, msg_seqno);

        //message_data_length 4
        byte[] message_data_length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(12).array();
        System.out.println("message_data_length: " + Helpers.bytesToHex(message_data_length));
        data = Helpers.concat(data, message_data_length);

        //7abe77ec 4
        byte[] message_data = Helpers.serialize_string("ping ping_id:long = Pong");
        System.out.println("7abe77ec: " + Helpers.bytesToHex(message_data));
        data = Helpers.concat(data, message_data);

        //ping_id 8
        byte[] ping_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(1).array();
        System.out.println("ping_id: " + Helpers.bytesToHex(ping_id));
        data = Helpers.concat(data, ping_id);

        System.out.println("ping data length: " + data.length);



        return data;  //To change body of created methods use File | Settings | File Templates.
    }



    private static byte[] encrypt_data( byte[] data, byte[] auth_key, byte[] msg_key) throws NoSuchAlgorithmException {

        int x = 0;

        byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32)));

        byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8 + 0), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8 + 0)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8 + 0));

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

        byte[] encrypted_data = aes_ige.encrypt(data);
        //System.out.println("encrypted_data: " + Helpers.bytesToHex(encrypted_data));
        //System.out.println("encrypted_data length: " + encrypted_data.length);



        return encrypted_data;

        //return new byte[0];
    }


    private static byte[] decrypt_data( byte[] data, byte[] auth_key, byte[] msg_key) throws NoSuchAlgorithmException {

        int x = 0;

        //byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32)));
        byte[] sha1_a = Helpers.hexStringToByteArray(Helpers.SHAsum(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32))));


        //byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_b = Helpers.hexStringToByteArray(Helpers.SHAsum(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x ))));


        //byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_c = Helpers.hexStringToByteArray(Helpers.SHAsum(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key)));

        //byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] sha1_d = Helpers.hexStringToByteArray(Helpers.SHAsum(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x))));

        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8 + 0), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8 + 0)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8 + 0));

        if(data.length % 16 != 0)
        {
            System.out.println("WARNING data.length % 16 != 0 !!!!");
            int i = data.length + 1;

            while(i % 16 != 0){
                i = i + 1;
            }

            System.out.println("data.length " + data.length + " increase to " + i);
            byte[] tmp = new byte[i];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }

        byte[] iv_1 = Helpers.substr(aes_iv, 0, 16);
        byte[] iv_2 = Helpers.substr(aes_iv, 16, 32);

        AES_IGE aes_ige = new AES_IGE(aes_key, iv_1, iv_2);

        byte[] encrypted_data = aes_ige.encrypt(data);
        System.out.println("encrypted_data: " + Helpers.bytesToHex(encrypted_data));
        System.out.println("encrypted_data length: " + encrypted_data.length);



        return encrypted_data;

        //return new byte[0];
    }
}
