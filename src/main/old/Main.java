package com.tl;


import com.skvortsov.mtproto.Transport;
import com.factor.PollardRho;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Main {

    private static BigInteger bi_b2;
    private static BigInteger bi_dhPrime2;

    public static void main(String[] args) {
        try
        {
            Transport sender = new Transport("95.142.192.65", 80);
            sender.connect();


            Packet req_pq = create_message_req_pq();
            req_pq.preview("Message req_pq: ");


            Packet resPQ = sender.send(req_pq, 96);
            resPQ.preview("Message resPQ: ");

            //System.out.println(resPQ.get_buffer().limit());

            byte[] buf = resPQ.get_as_bytes_BE(8 + 1 + 56, 8);
            byte[] buf2 = resPQ.get_as_bytes_BE(8 + 56, 12);
            byte[] nonce = resPQ.get_as_bytes_BE(8 + 24, 16);
            byte[] server_nonce = resPQ.get_as_bytes_BE(8 + 40, 16);

            System.out.println("nonce as byte array: " + Helpers.bytesToHex(nonce));
            System.out.println("server_nonce as byte array: " + Helpers.bytesToHex(server_nonce));
            System.out.println("pq as byte array: " + Helpers.bytesToHex(buf2));

            BigInteger pq = new BigInteger(buf);

            System.out.println("pq as BigInt: " + pq.toString());

            //факторизация
            PollardRho prho = new PollardRho();
            prho.factor(pq);
            BigInteger p = prho.getfactors2()[0];
            BigInteger q = prho.getfactors2()[1];
            prho.clear();

            int res = p.compareTo(q);
            if (res == 1)
            {
                System.out.println("WARNING p > q !!!!");
            }

            System.out.println("p as BigInt: " + p.toString());
            System.out.println("q as BigInt: " + q.toString());



            Packet p_q_inner_data = create_p_q_inner_data(resPQ, p, q);
            p_q_inner_data.preview("p_q_inner_data: ");

            ByteBuffer encrypted_data = create_encrypted_data(p_q_inner_data, pq);
            System.out.println("encrypted_data: " + Helpers.bytesToHex(encrypted_data.array()));



            //System.out.println("length: " + Helpers.bytesToHex(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(256).array()));


            Packet req_DH_params = create_message_req_DH_params(nonce, server_nonce, p_q_inner_data, resPQ, encrypted_data);
            req_DH_params.preview("Message req_DH_params: ");

            //byte[] buf4 = req_DH_params.get_as_bytes_BE(80 + 8 , 260);
            Packet server_DH_params = sender.send(req_DH_params, 56 + 596 + 12);
            server_DH_params.preview("Message server_DH_params: ");

            //Получение answer из encrypted_answer:
            //encrypted_answer
            //byte[] encrypted_answer = server_DH_params.get_as_bytes_BE(56 + 8, 592);
            byte[] encrypted_answer = server_DH_params.get_as_bytes_BE(56 + 8 + 4, 592);
            //byte[] encrypted_answer = server_DH_params.get_as_bytes_BE(56 + 8 + 4 + 4, 592);
            System.out.println("encrypted_answer : " + Helpers.bytesToHex(encrypted_answer));
            System.out.println("encrypted_answer length: " + Helpers.bytesToHex(encrypted_answer).length());
            //System.out.println("encrypted_answer length: " + new String("28A92FE20173B347A8BB324B5FAB2667C9A8BBCE6468D5B509A4CBDDC186240AC912CF7006AF8926DE606A2E74C0493CAA57741E6C82451F54D3E068F5CCC49B4444124B9666FFB405AAB564A3D01E67F6E912867C8D20D9882707DC330B17B4E0DD57CB53BFAAFA9EF5BE76AE6C1B9B6C51E2D6502A47C883095C46C81E3BE25F62427B585488BB3BF239213BF48EB8FE34C9A026CC8413934043974DB03556633038392CECB51F94824E140B98637730A4BE79A8F9DAFA39BAE81E1095849EA4C83467C92A3A17D997817C8A7AC61C3FF414DA37B7D66E949C0AEC858F048224210FCC61F11C3A910B431CCBD104CCCC8DC6D29D4A5D133BE639A4C32BBFF153E63ACA3AC52F2E4709B8AE01844B142C1EE89D075D64F69A399FEB04E656FE3675A6F8F412078F3D0B58DA15311C1A9F8E53B3CD6BB5572C294904B726D0BE337E2E21977DA26DD6E33270251C2CA29DFCC70227F0755F84CFDA9AC4B8DD5F84F1D1EB36BA45CDDC70444D8C213E4BD8F63B8AB95A2D0B4180DC91283DC063ACFB92D6A4E407CDE7C8C69689F77A007441D4A6A8384B666502D9B77FC68B5B43CC607E60A146223E110FCB43BC3C942EF981930CDC4A1D310C0B64D5E55D308D863251AB90502C3E46CC599E886A927CDA963B9EB16CE62603B68529EE98F9F5206419E03FB458EC4BD9454AA8F6BA777573CC54B328895B1DF25EAD9FB4CD5198EE022B2B81F388D281D5E5BC580107CA01A50665C32B552715F335FD76264FAD00DDD5AE45B94832AC79CE7C511D194BC42B70EFA850BB15C2012C5215CABFE97CE66B8D8734D0EE759A638AF013").length());
            //byte[] nonce_from_p_q_inner_data = p_q_inner_data.get_as_bytes_BE(32, 16);
            //byte[] server_nonce_from_p_q_inner_data = p_q_inner_data.get_as_bytes_BE(48, 16);
            byte[] new_nonce = p_q_inner_data.get_as_bytes_BE(64, 32);
            System.out.println("new_nonce : " + Helpers.bytesToHex(new_nonce));

            ByteBuffer new_nonce_plus_server_nonce = ByteBuffer.allocate(32 + 16);
            new_nonce_plus_server_nonce.put(new_nonce);
            new_nonce_plus_server_nonce.put(server_nonce);

            ByteBuffer server_nonce_plus_new_nonce = ByteBuffer.allocate(16 + 32);
            server_nonce_plus_new_nonce.put(server_nonce);
            server_nonce_plus_new_nonce.put(new_nonce);

            ByteBuffer new_nonce_plus_new_nonce = ByteBuffer.allocate(32 + 32);
            new_nonce_plus_new_nonce.put(new_nonce);
            new_nonce_plus_new_nonce.put(new_nonce);

            String sha_new_nonce_plus_server_nonce = Helpers.SHAsum(new_nonce_plus_server_nonce.array());
            System.out.println("sha_new_nonce_plus_server_nonce : " + sha_new_nonce_plus_server_nonce);
            System.out.println("sha_new_nonce_plus_server_nonce length: " + sha_new_nonce_plus_server_nonce.length());

            String sha_server_nonce_plus_new_nonce = Helpers.SHAsum(server_nonce_plus_new_nonce.array());
            System.out.println("sha_server_nonce_plus_new_nonce : " + sha_server_nonce_plus_new_nonce);
            System.out.println("sha_server_nonce_plus_new_nonce length: " + sha_server_nonce_plus_new_nonce.length());

            String sha_new_nonce_plus_new_nonce = Helpers.SHAsum(new_nonce_plus_new_nonce.array());
            System.out.println("sha_new_nonce_plus_new_nonce : " + sha_new_nonce_plus_new_nonce);
            System.out.println("sha_new_nonce_plus_new_nonce length: " + sha_new_nonce_plus_new_nonce.length());


            String tmp_aes_key_str = sha_new_nonce_plus_server_nonce + sha_server_nonce_plus_new_nonce.substring(0, 24);
            System.out.println("tmp_aes_key_str: " + tmp_aes_key_str);
            System.out.println("tmp_aes_key_str length: " + tmp_aes_key_str.length());


            String tmp_aes_iv_str = sha_server_nonce_plus_new_nonce.substring(24, 40) + sha_new_nonce_plus_new_nonce + Helpers.bytesToHex(new_nonce).substring(0, 8);
            System.out.println("tmp_aes_iv_str: " + tmp_aes_iv_str);
            System.out.println("tmp_aes_iv_str length: " + tmp_aes_iv_str.length());

            System.out.println("tmp_aes_iv1: " + tmp_aes_iv_str.substring(0, 32));
            System.out.println("tmp_aes_iv2: " + tmp_aes_iv_str.substring(32, 64));

            byte[] key = Helpers.hexStringToByteArray(tmp_aes_key_str);
            byte[] iv_1 = Helpers.hexStringToByteArray(tmp_aes_iv_str.substring(0, 32));
            byte[] iv_2 = Helpers.hexStringToByteArray(tmp_aes_iv_str.substring(32, 64));

            //!!!!!!!!!!!!!!!!!!!!
            ByteBuffer decrypted_answer = decrypt_answer(encrypted_answer, key, iv_1, iv_2);
            Packet temp_for_decrypted_answer = new Packet(decrypted_answer);


            //Разбор Server_DH_inner_data
            byte[] constr = temp_for_decrypted_answer.get_as_bytes_BE(20 + 0, 4);
            System.out.println("%(server_DH_inner_data): " + Helpers.bytesToHex(constr));

            byte[] g = temp_for_decrypted_answer.get_as_bytes_BE(20 + 36, 4);
            System.out.println("g: " + Helpers.bytesToHex(g));

            byte[] dh_prime = temp_for_decrypted_answer.get_as_bytes_BE(20 + 40 + 4, 256);
            System.out.println("dh_prime: " + Helpers.bytesToHex(dh_prime));
            System.out.println("dh_prime length: " + dh_prime.length);

            byte[] g_a = temp_for_decrypted_answer.get_as_bytes_BE(20 + 300 + 4, 256);
            //byte[] g_a = temp_for_decrypted_answer.get_as_bytes_BE(20 + 300, 260);
            System.out.println("g_a: " + Helpers.bytesToHex(g_a));
            System.out.println("g_a length: " + g_a.length);

            byte[] server_time = temp_for_decrypted_answer.get_as_bytes_BE(20 + 560, 4);
            System.out.println("server_time: " + Helpers.bytesToHex(server_time));


            //!!!!!!
            Packet client_DH_inner_data = create_client_DH_inner_data(g, dh_prime, nonce, server_nonce);
            client_DH_inner_data.preview("client_DH_inner_data: ");
            System.out.println("client_DH_inner_data length: " + client_DH_inner_data.get_buffer().array().length);

            byte[] encrypted_client_DH_inner_data = encrypt_client_DH_inner_data(client_DH_inner_data, key, iv_1, iv_2);


            Packet set_client_DH_params = create_message_set_client_DH_params(encrypted_client_DH_inner_data, nonce, server_nonce);
            set_client_DH_params.preview("set_client_DH_params: ");
            //System.out.println("set_client_DH_params length: " + client_DH_inner_data.get_buffer().array().length);
            Packet dh_gen_ = sender.send(set_client_DH_params, 84);
            dh_gen_.preview("dh_gen_: ");


            //Вычисляется auth_key
            BigInteger bi_g_a = new BigInteger(1, g_a);
            //BigInteger bi_g_a = new BigInteger(1, g);
            BigInteger bi_auth_key = bi_g_a.modPow(bi_b2, bi_dhPrime2);
            byte[] auth_key = bi_auth_key.toByteArray();

            if (auth_key[0] == 0) {

                System.out.println("WARNING auth_key length == 257 !!!!");
                byte[] tmp = new byte[auth_key.length - 1];
                System.arraycopy(auth_key, 1, tmp, 0, tmp.length);
                auth_key = tmp;
            }

            //Авторизационный ключ
            System.out.println("auth_key: " + Helpers.bytesToHex(auth_key));
            System.out.println("auth_key length: " + auth_key.length);

            //Разбор Set_client_DH_params_answer
            byte[] dh_gen  = dh_gen_.get_as_bytes_BE(4 + 4 + 8 + 8 + 4, 4);
            System.out.println("dh_gen_ok?: " + Helpers.bytesToHex(dh_gen));



            //???
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
            byte[] array_1 = Arrays.copyOfRange(server_nonce, 0, 8);
            byte[] array_2 = Arrays.copyOfRange(new_nonce, 0, 8);

            byte[] salt = new byte[8];

            int i = 0;
            for (byte b : array_1)
                salt[i] = (byte) (b ^ array_2[i++]);


            //byte[] salt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(1).array();
            salt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(salt).array();
            System.out.println("salt: " + Helpers.bytesToHex(salt));

            //1) create_data_for_ping
            //byte[] data_for_ping = create_data_for_ping(salt);
            //System.out.println("data_for_ping: " + Helpers.bytesToHex(data_for_ping));

            //1) create_data_for_save_developer_info
            byte[] data_for_ping = create_data_for_save_developer_info(salt);
            System.out.println("data_for_save_developer_info: " + Helpers.bytesToHex(data_for_ping));

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
            packet_numb_for_tcp = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(3).array();
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

            //разбор ответа
            byte[] answer;
            answer = sender.send(result3);
            System.out.println("answer: " + Helpers.bytesToHex(answer));

            byte[] b_legth_answer = Helpers.conv_from_LE_to_BE(Helpers.substr(answer, 0, 4));
            System.out.println("b_legth_answer: " + Helpers.bytesToHex(b_legth_answer));
            ByteBuffer wrapped = ByteBuffer.wrap(b_legth_answer);
            int i_length = wrapped.getInt();
            System.out.println("i_length: " + i_length);

            answer = Helpers.substr(answer, 0, i_length);
            System.out.println("answer: " + Helpers.bytesToHex(answer));


            //encrypted_data
            byte[] encrypted_data3 = Helpers.substr(answer, 4 + 4 + 8 + 16, answer.length - 4);
            System.out.println("encrypted_data3: " + Helpers.bytesToHex(encrypted_data3));

            //2) msgKey
            byte[] msgKey_otv = Helpers.substr(answer, 4 + 4 + 8, 4 + 4 + 8 + 16);
            System.out.println("msgKey_otv: " + Helpers.bytesToHex(msgKey_otv));
            //msgKey_otv = Helpers.conv_from_LE_to_BE(msgKey_otv);

            byte[] decrypted_answer3 = decrypt_data(encrypted_data3, auth_key, msgKey_otv);
            System.out.println("decrypted_answer3: " + Helpers.bytesToHex(decrypted_answer3));

            //something_else
            byte[] something_else;
            something_else = sender.wait_something_else();
            System.out.println("something_else: " + Helpers.bytesToHex(something_else));





            //разбор something_else
            byte[] b_legth_something_else = Helpers.conv_from_LE_to_BE(Helpers.substr(something_else, 0, 4));
            System.out.println("b_legth_something_else: " + Helpers.bytesToHex(b_legth_something_else));
            ByteBuffer wrapped2 = ByteBuffer.wrap(b_legth_something_else);
            int i_length2 = wrapped2.getInt();
            System.out.println("i_length2: " + i_length2);

            something_else = Helpers.substr(something_else, 0, i_length2);
            System.out.println("something_else: " + Helpers.bytesToHex(something_else));


            //encrypted_data
            byte[] encrypted_data4 = Helpers.substr(something_else, 4 + 4 + 8 + 16, something_else.length - 4);
            System.out.println("encrypted_data4: " + Helpers.bytesToHex(encrypted_data4));

            //2) msgKey
            byte[] msgKey_otv2 = Helpers.substr(something_else, 4 + 4 + 8, 4 + 4 + 8 + 16);
            System.out.println("msgKey_otv2: " + Helpers.bytesToHex(msgKey_otv2));
            //msgKey_otv = Helpers.conv_from_LE_to_BE(msgKey_otv);

            byte[] decrypted_answer4 = decrypt_data(encrypted_data4, auth_key, msgKey_otv2);
            System.out.println("decrypted_answer4: " + Helpers.bytesToHex(decrypted_answer4));


            byte[] qwerty = Helpers.substr(decrypted_answer4, 48, 48 + 4);
            System.out.println("qwerty: " + Helpers.bytesToHex(qwerty));

            sender.disconnect();

        }
        catch(Exception e)
        {System.out.println("init error: "+e);} // вывод исключений

    }

    private static byte[] create_data_for_save_developer_info(byte[] salt) {


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

        byte[] mes_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array();
        //byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
        System.out.println("message_id: " + Helpers.bytesToHex(mes_id));


        data = Helpers.concat(data, mes_id);

        //msg_seqno 4
        byte[] msg_seqno = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
        System.out.println("msg_seqno: " + Helpers.bytesToHex(msg_seqno));
        data = Helpers.concat(data, msg_seqno);

        //message_data_length 4
        byte[] message_data_length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4 + 4 + 20 + 16 + 4 + 16).array();
        //byte[] message_data_length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(4 + 4 + 32 + 16 + 4 + 16).array();
        System.out.println("message_data_length: " + Helpers.bytesToHex(message_data_length));
        data = Helpers.concat(data, message_data_length);


        //byte[] dt = Helpers.hexStringToByteArray("956e5f9a3930000012d090d0bbd0b5d0bad181d0b0d0bdd0b4d180000d2b33383039333132333435363700001200000008d09ad0b8d0b5d0b2000000");
        //  byte[] dt = Helpers.hexStringToByteArray("956e5f9aACE27F0112d090d0bbd0b5d0bad181d0b0d0bdd0b4d180000d2b33383039333132333435363700001200000008d09ad0b8d0b5d0b2000000");
        byte[] dt = Helpers.hexStringToByteArray("956E5F9AACE27F0112D0A1D0B5D180D0B3D0B5D0B9202020202020000d2B37393035363632343135352000001A0000000CD09CD0BED181D0BAD0B2D0B0000000");
        data = Helpers.concat(data, dt);

        /*
        //9a5f6e95
        byte[] message_data = Helpers.serialize_string("contest.saveDeveloperInfo vk_id:int name:string phone_number:string age:int city:string = Bool");
        System.out.println("9a5f6e95: " + Helpers.bytesToHex(message_data));
        data = Helpers.concat(data, message_data);

        //vk_id
        byte[] vk_id = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(25158316).array();
        //byte[] vk_id = ByteBuffer.allocate(4).putInt(25158316).array();
        System.out.println("vk_id: " + Helpers.bytesToHex(vk_id));
        data = Helpers.concat(data, vk_id);

        //name
        String name = new String("Скворцов Сергей");      //32
        //System.out.println("name: " + name.length());
        //data = Helpers.concat(data, string_to_byte_array(name));
        data = Helpers.concat(data, string_to_byte_array(name));

        //phone
        String phone = new String("+79056624155");      //16
        //System.out.println("phone as byte: " + Helpers.bytesToHex(string_to_byte_array(phone)));
        data = Helpers.concat(data, string_to_byte_array(phone));

        //age
        byte[] age = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(26).array();
        //System.out.println("age: " + Helpers.bytesToHex(age));
        data = Helpers.concat(data, age);

        //sity
        String sity = new String("Москва");      //16
        //System.out.println("name length: " + name.length());
        data = Helpers.concat(data, string_to_byte_array(sity));

        System.out.println("save_developer_info data length: " + data.length);
        System.out.println("save_developer_info data: " + Helpers.bytesToHex(data));

        */

        return data;  //To change body of created methods use File | Settings | File Templates.
    }


    public static byte[] string_to_byte_array(String name)
    {
        byte[] name_as_byte = new byte[]{};
        System.out.println("name.length: " +  name.getBytes().length);

        name_as_byte = ByteBuffer.allocate(1).put((byte) name.length()).array();


        //name_as_byte = Helpers.concat(name_as_byte, name.getBytes(Charset.forName("UTF-8")));
        name_as_byte = Helpers.concat(name_as_byte, name.getBytes());
        System.out.println("name as byte: " + Helpers.bytesToHex(name_as_byte));
        System.out.println("name as byte length: " + name_as_byte.length);

        if (name_as_byte.length % 4 != 0)
        {
            System.out.println("WARNING name_as_byte.length % 4 != 0 !!!!");
            int i = name_as_byte.length + 1;

            while(i % 4 != 0){
                i = i + 1;
            }

            System.out.println("name_as_byte length " + name_as_byte.length + " increase to " + i);
            byte[] tmp = new byte[i];
            System.arraycopy(name_as_byte, 0, tmp, 0, name_as_byte.length);
            name_as_byte = tmp;
            System.out.println("new name_as_byte " + Helpers.bytesToHex(name_as_byte));
            System.out.println("new name_as_byte length " + name_as_byte.length);
        }

        return name_as_byte;
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

        byte[] mes_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array();
        //byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
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


        int x = 8;

        byte[] sha1_a = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, x, 32 + x)));

        byte[] sha1_b = Helpers.SHA1(Helpers.concat(Helpers.concat(Helpers.substr(auth_key, 32 + x, 16 + 32 + x), msg_key), Helpers.substr(auth_key, 48 + x, 16 + 48 + x )));
        byte[] sha1_c = Helpers.SHA1(Helpers.concat(Helpers.substr(auth_key, 64 + x, 32 + 64 + x), msg_key));
        byte[] sha1_d = Helpers.SHA1(Helpers.concat(msg_key, Helpers.substr(auth_key, 96 + x, 32 + 96 + x)));
        byte[] aes_key = Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 0, 8 + 0), Helpers.substr(sha1_b, 8, 12 + 8)), Helpers.substr(sha1_c, 4, 12 + 4));
        byte[] aes_iv = Helpers.concat(Helpers.concat(Helpers.concat(Helpers.substr(sha1_a, 8, 12 + 8), Helpers.substr(sha1_b, 0, 8 + 0)), Helpers.substr(sha1_c, 16, 4 + 16)), Helpers.substr(sha1_d, 0, 8 + 0));

        /*if(data.length % 16 != 0)
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
        */

        System.out.println("data " + Helpers.bytesToHex(data));
        byte[] iv_1 = Helpers.substr(aes_iv, 0, 16);
        byte[] iv_2 = Helpers.substr(aes_iv, 16, 32);

        AES_IGE aes_ige = new AES_IGE(aes_key, iv_1, iv_2);

        byte[] decrypted_data = aes_ige.decrypt(data);
        //System.out.println("encrypted_data: " + Helpers.bytesToHex(encrypted_data));
        //System.out.println("encrypted_data length: " + encrypted_data.length);



        return decrypted_data;

        //return new byte[0];
    }




    private static byte[] encrypt_client_DH_inner_data(Packet client_dh_inner_data, byte[] key, byte[] iv_1, byte[] iv_2) throws NoSuchAlgorithmException {


        ByteBuffer data_with_hash = ByteBuffer.allocate(20 + 304 + 12);

        byte[] sha1_data = Helpers.hexStringToByteArray(Helpers.SHAsum(client_dh_inner_data.get_buffer().array()));
        data_with_hash.put(sha1_data);

        byte[] data = client_dh_inner_data.get_buffer().array();
        data_with_hash.put(data);

        System.out.println("SHA1 (data) = " + Helpers.bytesToHex(sha1_data));
        System.out.println("SHA1 (data) length = " + sha1_data.length);
        System.out.println("data: " + Helpers.bytesToHex(data));
        System.out.println("data length: " + data.length);
        System.out.println("data with hash: " + Helpers.bytesToHex(data_with_hash.array()));
        System.out.println("data with hash length: " + data_with_hash.array().length);


        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);

        byte[] encrypted_data = aes_ige.encrypt(data_with_hash.array());
        System.out.println("encrypted_data: " + Helpers.bytesToHex(encrypted_data));
        System.out.println("encrypted_data length: " + encrypted_data.length);



        return encrypted_data;  //To change body of created methods use File | Settings | File Templates.
    }



    private static Packet create_client_DH_inner_data(byte[] g, byte[] dhPrime, byte[] nonce, byte[] server_nonce) {

        Packet packet;
        packet = new Packet(44 + 260, Boolean.TRUE);

        //%(client_DH_inner_data) 4
        byte[] client_DH_inner_data = Helpers.serialize_string("client_DH_inner_data nonce:int128 server_nonce:int128 retry_id:long g_b:string = Client_DH_Inner_Data");
        packet.set(client_DH_inner_data);

        //nonce 16
        packet.set(nonce);

        //server_nonce 16
        packet.set(server_nonce);

        //retry_id  8
        byte[] retry_id = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        packet.set(retry_id);

        //g_b  260
        byte[] b = new byte[256];
        new Random().nextBytes(b);

        g = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(2).array();
        System.out.println("g!!!!: " + Helpers.bytesToHex(g));

        BigInteger bi_g = new BigInteger(1, g);
        BigInteger bi_b = new BigInteger(1, b);
        bi_b2 = bi_b;
        BigInteger bi_dhPrime = new BigInteger(1, dhPrime);
        bi_dhPrime2 = bi_dhPrime;


        BigInteger bi_g_b = bi_g.modPow(bi_b, bi_dhPrime);
        byte[] array = bi_g_b.toByteArray();

        System.out.println(array.length);

        if (array[0] == 0) {

            System.out.println("WARNING length == 257 !!!!");
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }

        ByteBuffer res = ByteBuffer.allocate(260);


        res.put((byte)0xFE);
        res.put((byte)0x00);
        res.put((byte)0x01);
        res.put((byte)0x00);

        res.put(array);

        //итого - 304
        packet.set(res.array());


        return packet;

    }

    private static Packet create_message_set_client_DH_params(byte[] encrypted_client_dh_inner_data, byte[] nonce, byte[] server_nonce) {

        Packet packet;
        packet = new Packet(396 + 12);
        // packet = new Packet(52);

        //порядковый номер сообщения
        byte[] num = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(2).array();
        packet.set(num);

        //auth_key_id
        byte[] ak = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        packet.set(ak);

        //message_id
        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
        packet.set(mes_id);

        //message_length
        byte[] length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(376).array();
        packet.set(length);

        //%(set_client_DH_params)
        byte[] ss = Helpers.serialize_string("set_client_DH_params nonce:int128 server_nonce:int128 encrypted_data:string = Set_client_DH_params_answer");
        packet.set(ss);

        //nonce
        packet.set(nonce);

        //server_nonce
        packet.set(server_nonce);

        //encrypted_client_dh_inner_data length
        //packet.set(new byte[] {(byte)0xFE, (byte)0x50, (byte)0x01, (byte)0x00});
        packet.set(Helpers.hexStringToByteArray("FE500100"));

        //encrypted_client_dh_inner_data
        packet.set(encrypted_client_dh_inner_data);

        //crc32
        packet.set_crc32();

        //собираем сообщение, дописываем заголовки и crc32 (+ 12 байтовк к длинне)
        //packet.pack();

        return packet;


    }

    private static ByteBuffer decrypt_answer(byte[] encrypted_answer, byte[] key, byte[] iv_1, byte[] iv_2) {


        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);

        byte[] answer_with_hash = aes_ige.decrypt(encrypted_answer);
        System.out.println("decrypted_answer: " + Helpers.bytesToHex(answer_with_hash));
        System.out.println("decrypted_answer length: " + answer_with_hash.length);
        ByteBuffer answer = ByteBuffer.allocate(answer_with_hash.length);
        answer.put(answer_with_hash);

        return answer;  //To change body of created methods use File | Settings | File Templates.
    }




    private static void decrypt_test_answer() {

        /*byte[] key = Helpers.hexStringToByteArray("000102030405060708090A0B0C0D0E0F");
        byte[] iv_1 = Helpers.hexStringToByteArray("000102030405060708090A0B0C0D0E0F");
        byte[] iv_2 = Helpers.hexStringToByteArray("101112131415161718191A1B1C1D1E1F");
        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);
        byte[] result = aes_ige.encrypt(Helpers.hexStringToByteArray("0000000000000000000000000000000000000000000000000000000000000000"));
        System.out.println(Helpers.bytesToHex(result));
        byte[] result2 = aes_ige.decrypt(Helpers.hexStringToByteArray("1A8519A6557BE652E9DA8E43DA4EF4453CF456B4CA488AA383C79C98B34797CB"));
        System.out.println(Helpers.bytesToHex(result2)); */

        byte[] key = Helpers.hexStringToByteArray("F011280887C7BB01DF0FC4E17830E0B91FBB8BE4B2267CB985AE25F33B527253");
        //byte[] iv_1 = Helpers.hexStringToByteArray("3212D579EE35452ED23E0D0C92841AA7");
        //byte[] iv_2 = Helpers.hexStringToByteArray("D31B2E9BDEF2151E80D15860311C85DB");
        byte[] iv_1 = Helpers.hexStringToByteArray("3212D579EE35452ED23E0D0C92841AA7");
        byte[] iv_2 = Helpers.hexStringToByteArray("D31B2E9BDEF2151E80D15860311C85DB");
        AES_IGE aes_ige = new AES_IGE(key, iv_1, iv_2);
        //byte[] result = aes_ige.encrypt(Helpers.hexStringToByteArray("0000000000000000000000000000000000000000000000000000000000000000"));
        //System.out.println(Helpers.bytesToHex(result));
        //AES_IGE aes_ige2 = new AES_IGE(key, iv_2, iv_1);
        byte[] result2 = aes_ige.decrypt(Helpers.hexStringToByteArray("28A92FE20173B347A8BB324B5FAB2667C9A8BBCE6468D5B509A4CBDDC186240AC912CF7006AF8926DE606A2E74C0493CAA57741E6C82451F54D3E068F5CCC49B4444124B9666FFB405AAB564A3D01E67F6E912867C8D20D9882707DC330B17B4E0DD57CB53BFAAFA9EF5BE76AE6C1B9B6C51E2D6502A47C883095C46C81E3BE25F62427B585488BB3BF239213BF48EB8FE34C9A026CC8413934043974DB03556633038392CECB51F94824E140B98637730A4BE79A8F9DAFA39BAE81E1095849EA4C83467C92A3A17D997817C8A7AC61C3FF414DA37B7D66E949C0AEC858F048224210FCC61F11C3A910B431CCBD104CCCC8DC6D29D4A5D133BE639A4C32BBFF153E63ACA3AC52F2E4709B8AE01844B142C1EE89D075D64F69A399FEB04E656FE3675A6F8F412078F3D0B58DA15311C1A9F8E53B3CD6BB5572C294904B726D0BE337E2E21977DA26DD6E33270251C2CA29DFCC70227F0755F84CFDA9AC4B8DD5F84F1D1EB36BA45CDDC70444D8C213E4BD8F63B8AB95A2D0B4180DC91283DC063ACFB92D6A4E407CDE7C8C69689F77A007441D4A6A8384B666502D9B77FC68B5B43CC607E60A146223E110FCB43BC3C942EF981930CDC4A1D310C0B64D5E55D308D863251AB90502C3E46CC599E886A927CDA963B9EB16CE62603B68529EE98F9F5206419E03FB458EC4BD9454AA8F6BA777573CC54B328895B1DF25EAD9FB4CD5198EE022B2B81F388D281D5E5BC580107CA01A50665C32B552715F335FD76264FAD00DDD5AE45B94832AC79CE7C511D194BC42B70EFA850BB15C2012C5215CABFE97CE66B8D8734D0EE759A638AF013"));
        System.out.println(Helpers.bytesToHex(result2));


    }

    private static Packet create_message_req_DH_params(byte[] nonce, byte[] server_nonce, Packet p_q_inner_data, Packet resPQ, ByteBuffer encrypted_data) {

        Packet packet;
        packet = new Packet(340 + 12);
       // packet = new Packet(52);

        //порядковый номер сообщения
        byte[] num = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
        packet.set_num(num);

        //auth_key_id
        byte[] ak = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        packet.set_auth_key_id(ak);

        //message_id
        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
        packet.set_message_id(mes_id);

        //message_length
        byte[] length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(320).array();
        packet.set_message_length(length);

        //%(req_DH_params) #d712e4be
        byte[] ss = Helpers.serialize_string("req_DH_params nonce:int128 server_nonce:int128 p:string q:string public_key_fingerprint:long encrypted_data:string = Server_DH_Params");
        packet.set_constructor_name(ss);

        //nonce
        packet.set_nonce(nonce);

        //server_nonce
        packet.set_server_nonce(server_nonce);

        //p
        packet.set_p(p_q_inner_data.get_p_as_byte_array());

        //q
        packet.set_q(p_q_inner_data.get_q_as_byte_array());

        //public_key_fingerprint
        byte[] buf = resPQ.get_as_bytes_BE(8 + 76, 8);
        packet.set_public_key_fingerprint(buf);

        //encrypted_data
        packet.set_encrypted_data(encrypted_data.array());

        //crc32
        packet.set_crc32();

        //собираем сообщение, дописываем заголовки и crc32 (+ 12 байтовк к длинне)
        //packet.pack();

        return packet;



    }

    private static ByteBuffer create_encrypted_data(Packet p_q_inner_data, BigInteger pq) throws NoSuchAlgorithmException {



        byte[] b = p_q_inner_data.get_buffer().array();

        ByteBuffer data_with_hash = ByteBuffer.allocate(255);

        byte[] sha1_data = Helpers.hexStringToByteArray(Helpers.SHAsum(b));
        data_with_hash.put(sha1_data);

        byte[] data = p_q_inner_data.get_buffer().array();
        data_with_hash.put(data);

        System.out.println("SHA1 (data) = " + Helpers.bytesToHex(sha1_data));
        System.out.println("data: " + Helpers.bytesToHex(data));
        System.out.println("data with hash: " + Helpers.bytesToHex(data_with_hash.array()));

        //BigInteger bi = new BigInteger(data_with_hash.array());

        //rsa, m (модуль) - pq, экспонента - 010001 r - сообщение


        BigInteger e = new BigInteger("010001", 16);
        BigInteger r = new BigInteger(1, data_with_hash.array());
        BigInteger m = new BigInteger("C150023E2F70DB7985DED064759CFECF0AF328E69A41DAF4D6F01B538135A6F91F8F8B2A0EC9BA9720CE352EFCF6C5680FFC424BD634864902DE0B4BD6D49F4E580230E3AE97D95C8B19442B3C0A10D8F5633FECEDD6926A7F6DAB0DDB7D457F9EA81B8465FCD6FFFEED114011DF91C059CAEDAF97625F6C96ECC74725556934EF781D866B34F011FCE4D835A090196E9A5F0E4449AF7EB697DDB9076494CA5F81104A305B6DD27665722C46B60E5DF680FB16B210607EF217652E60236C255F6A28315F4083A96791D7214BF64C1DF4FD0DB1944FB26A2A57031B32EEE64AD15A8BA68885CDE74A5BFC920F6ABF59BA5C75506373E7130F9042DA922179251F",16);
        System.out.println("r: " + r);
        System.out.println("e: " + e);
        System.out.println("pq: " + pq);
        System.out.println("m: " + m);

        BigInteger s = r.modPow(e, m);
        System.out.println("s: " + s);

        byte[] array = s.toByteArray();

        System.out.println(array.length);

        if (array[0] == 0) {

            System.out.println("WARNING length == 257 !!!!");
            byte[] tmp = new byte[array.length - 1];
            System.arraycopy(array, 1, tmp, 0, tmp.length);
            array = tmp;
        }

        //RSA (data_with_hash, server_public_key);
        ByteBuffer res = ByteBuffer.allocate(260);


        //System.out.println(Helpers.bytesToHex(bytes));
        //System.out.println(bytes.length);


        //res.put(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(256).array());
        res.put((byte)0xFE);
        res.put((byte)0x00);
        res.put((byte)0x01);
        res.put((byte)0x00);

        res.put(array);

        return res;  //To change body of created methods use File | Settings | File Templates.
    }


    public static Packet create_p_q_inner_data(Packet resPQ, BigInteger biP, BigInteger biQ)
    {
        Packet packet;
        packet = new Packet(96, Boolean.TRUE);


        byte[] p_q_inner_data = Helpers.serialize_string("p_q_inner_data pq:string p:string q:string nonce:int128 server_nonce:int128 new_nonce:int256 = P_Q_inner_data");
        packet.set_p_q_inner_data(p_q_inner_data);


        packet.set_pq(resPQ.get_as_bytes_BE(8 + 56, 12));

        packet.set_p(biP);

        packet.set_q(biQ);

        packet.set_nonce(resPQ.get_as_bytes_BE(8 + 24, 16));
        packet.set_server_nonce(resPQ.get_as_bytes_BE(8 + 40, 16));

        //new_nonce
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        packet.set_new_nonce(b);

        return packet;
    }

    public static Packet create_message_req_pq()
    {

        Packet packet;
        packet = new Packet(52);

        //порядковый номер сообщения
        byte[] num = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        packet.set_num(num);

        //auth_key_id
        byte[] ak = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array();
        packet.set_auth_key_id(ak);

        //message_id
        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        byte[] mes_id = ByteBuffer.allocate(8).putLong(unixTime).array();
        packet.set_message_id(mes_id);

        //message_length
        byte[] length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(20).array();
        packet.set_message_length(length);

        //%(req_pq)
        byte[] ss = Helpers.serialize_string("req_pq nonce:int128 = ResPQ");
        packet.set_constructor_name(ss);

        //nonce
        byte[] b = new byte[16];
        new Random().nextBytes(b);
        packet.set_nonce(b);

        //crc32
        packet.set_crc32();

        //собираем сообщение, дописываем заголовки и crc32 (+ 12 байтовк к длинне)
        //packet.pack();

        return packet;

        //packet.send();

    }





}
