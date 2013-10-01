
package com.skvortsov.mtproto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Constructor implements Cloneable{
   	private String id;
   	private List<Param> params;
   	private String predicate;
   	private String type;

 	public String getId(){
		return this.id;
	}

	public void setId(String id){
		this.id = id;
	}

 	public List<Param> getParams(){
		return this.params;
	}

	public void setParams(List<Param> params){
		this.params = params;
	}

 	public String getPredicate(){
		return this.predicate;
	}

	public void setPredicate(String predicate){
		this.predicate = predicate;
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

    public Constructor clone() throws CloneNotSupportedException{
        Constructor obj = (Constructor)super.clone();
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
        while(unixTime % 4 != 0){
            unixTime++;
        }
        d.setMessage_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array());
        d.setSeq_no(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SessionManager.getS().getNextSeqNo()).array());
        d.setMessage_data_length(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SerializationUtils.calcSize(this)).array());
        d.setMessage_data(SerializationUtils.serialize(this));

        SessionManager.saveSeq_no();

        return d;
    }

    public Message toMessage(){
        Message m = new Message();
        m.setAuth_key_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).array());

        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        while(unixTime % 4 != 0){
            unixTime++;
        }
        m.setMessage_id(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(unixTime).array());
        m.setMessage_data_length(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(SerializationUtils.calcSize(this)).array());
        m.setMessage_data(SerializationUtils.serialize(this));

        return m;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Construtor id: ").append(this.getId()).append("; ");
        sb.append("predicate: ").append(this.getPredicate()).append("; ");
        sb.append("type: ").append(this.getType()).append(";");
        sb.append("parameters: ");
        for (Param p : this.getParams()) {
            sb.append(p.toString());

        }


        return sb.toString();
    }
}
