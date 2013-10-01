package com.tl;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 20.07.13
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */




public class TL {


    private final HashMap<String, TLTypeConstructor> constructorsByName;
    private String s;
    private Map<String, Object> data;
    private boolean b;


    private final HashMap<Integer, TLTypeConstructor> constructorsById;

        public TL() {
        this.constructorsById = new HashMap<Integer, TLTypeConstructor>();
        this.constructorsByName = new HashMap<String, TLTypeConstructor>();

        this.autoCreate("req_pq#60469778 nonce:int128 = ResPQ");
        this.autoCreate("server_DH_inner_data#b5890dba nonce:int128 server_nonce:int128 g:int dh_prime:string g_a:string server_time:int = Server_DH_inner_data");
        this.autoCreate("p_q_inner_data#83c95aec pq:string p:string q:string nonce:int128 server_nonce:int128 new_nonce:int256 = P_Q_inner_data");
        this.autoCreate("req_DH_params#d712e4be nonce:int128 server_nonce:int128 p:string q:string public_key_fingerprint:long encrypted_data:string = Server_DH_Params");
        this.autoCreate("server_DH_params_fail#79cb045d nonce:int128 server_nonce:int128 new_nonce_hash:int128 = Server_DH_Params");
        this.autoCreate("server_DH_params_ok#d0e8075c nonce:int128 server_nonce:int128 encrypted_answer:string = Server_DH_Params");
        this.autoCreate("server_DH_inner_data#b5890dba nonce:int128 server_nonce:int128 g:int dh_prime:string g_a:string server_time:int = Server_DH_inner_data");
        this.autoCreate("client_DH_inner_data#6643b654 nonce:int128 server_nonce:int128 retry_id:long g_b:string = Client_DH_Inner_Data");
        this.autoCreate("set_client_DH_params#f5045f1f nonce:int128 server_nonce:int128 encrypted_data:string = Set_client_DH_params_answer");
        this.autoCreate("dh_gen_ok#3bcbf734 nonce:int128 server_nonce:int128 new_nonce_hash1:int128 = Set_client_DH_params_answer");
        this.autoCreate("dh_gen_retry#46dc1fb9 nonce:int128 server_nonce:int128 new_nonce_hash2:int128 = Set_client_DH_params_answer");
        this.autoCreate("dh_gen_fail#a69dae02 nonce:int128 server_nonce:int128 new_nonce_hash3:int128 = Set_client_DH_params_answer");

        // Полиморфный тип, примитивным парсингом не взять
        // resPQ#05162463 nonce:int128 server_nonce:int128 pq:string server_public_key_fingerprints:Vector long = ResPQ
        TLTypeConstructor resPQ05162463 = new TLTypeConstructor(0x05162463, "resPQ#05162463");

        resPQ05162463.addField(new TLMapPair("nonce", new TLBytesInstanceType(16)));
        resPQ05162463.addField(new TLMapPair("server_nonce", new TLBytesInstanceType(16)));
        resPQ05162463.addField(new TLMapPair("pq", new TLStringInstanceType()));

        resPQ05162463.addField(new TLMapPair("server_public_key_fingerprints",
                new TLVectorInstanceType(new TLLongInstanceType()).box()));

        this.constructorsById.put(resPQ05162463.getId(), resPQ05162463);
        this.constructorsByName.put(resPQ05162463.getName(), resPQ05162463);
    }

    private void autoCreate(String s)
    {

    }



    public byte[] Serialize(String s, Map<String, Object> data, boolean b)
    {


        byte[] res = (byte[])data.get("nonce");
        byte[] res2 = Helpers.hexStringToByteArray(s);

        for (int i = 0, j = res2.length - 1; i < j; i++, j--)
        {
            byte bb = res2[i];
            res2[i] = res2[j];
            res2[j] = bb;
        }


        ByteBuffer target = ByteBuffer.allocate(20);
        target.put(res2);
        target.put(res);

        return target.array();
    }


    public void addType(String s) {


        this.s = s;
    }
}


