
package com.skvortsov.mtproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Method implements Cloneable{
   	private String id;
   	private String method;
   	private List<Param> params;
   	private String type;
    private static final String TAG = "Method: ";
 	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
 	public String getMethod(){
		return this.method;
	}
	public void setMethod(String method){
		this.method = method;
	}
 	public List<Param> getParams(){
		return this.params;
	}
	public void setParams(List<Param> params){
		this.params = params;
	}
 	public String getType(){
		return this.type;
	}
	public void setType(String type){
		this.type = type;
	}

    public Param getParamByName(String name) {
        for (Param element : this.params) {
            if (element.getName().equals(name)) {
                return element;
            }
        }
        throw new RuntimeException("Cannot find Parameter with name: " + name);
    }
    public Method clone() throws CloneNotSupportedException{
        Method obj = (Method)super.clone();
        List<Param> lp = new ArrayList<Param>();
        for (Param p : obj.getParams()){
            lp.add((Param)p.clone());
        }
        obj.setParams(lp);
        return obj;
    }

    public Data toData(){

        Data d = new Data();

        d.setSalt(SessionManager.getS().getSalt());
        d.setSession_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(SessionManager.getS().getSession_id()).array());

        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        if(unixTime % 4 != 0){
            unixTime = unixTime + 1;
            while(unixTime % 4 != 0){
                unixTime = unixTime + 1;
            }
        }

        d.setMessage_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array());
        d.setSeq_no(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SessionManager.getS().getNextSeqNo()).array());
        d.setMessage_data_length(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SerializationUtils.calcSize(this)).array());
        System.out.println(TAG + "Message_data_length: " + Helpers.bytesToHex(d.getMessage_data_length()));
        d.setMessage_data(SerializationUtils.serialize(this));
        System.out.println(TAG + "Message_data: " + Helpers.bytesToHex(d.getMessage_data()));

        System.out.println(TAG + this.getMethod() + "(" + d.array().length + ")" + ": " + Helpers.bytesToHex(d.array()));

        SessionManager.saveSeq_no();

        return d;
    }

    public Message toMessage(){
        Message m = new Message();
        m.setAuth_key_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());

        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        if(unixTime % 4 != 0){
            unixTime = unixTime + 1;
            while(unixTime % 4 != 0){
                unixTime = unixTime + 1;
            }
        }
        m.setMessage_id(ByteBuffer.allocate(8).putLong(unixTime).array());
        m.setMessage_data_length(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SerializationUtils.calcSize(this)).array());
        m.setMessage_data(SerializationUtils.serialize(this));

        return m;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Method id: ").append(this.getId()).append("; ");
        sb.append("name: ").append(this.getMethod()).append("; ");
        sb.append("type: ").append(this.getType()).append(";");
        sb.append("parameters: ");
        for (Param p : this.getParams()) {
            sb.append(p.toString());

        }
        return sb.toString();
    }
}
