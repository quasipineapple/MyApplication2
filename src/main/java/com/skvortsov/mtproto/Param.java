
package com.skvortsov.mtproto;

public class Param implements Cloneable{
   	private String name;
   	private String type;
    private Object data;

 	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
 	public String getType(){
		return this.type;
	}
	public void setType(String type){
		this.type = type;
	}
    public Object getData(){
        return this.data;
    }
    public void setData(Object data){
        this.data = data;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {

        return super.clone();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("type: ").append(this.getType()).append(";");
        sb.append("name: ").append(this.getName()).append(";");
        if(this.getType().equals("string")){

            sb.append("data: ").append(Helpers.BytesToText((byte[]) this.getData())).append(";");

        }else if(this.getType().contains("Vector<DcOption>")){

            Constructor[] cc = (Constructor[]) this.getData();
            for(Constructor c : cc){
                sb.append(c.toString());
            }

        }else{
            sb.append("data: ").append(this.getData().toString()).append(";");
        }


        return sb.toString();
    }

}
