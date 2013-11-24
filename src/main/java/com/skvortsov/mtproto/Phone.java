package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/20/13.
 */
public class Phone {

    private Boolean phone_registered;
    private String phone_code_hash;
    private String phone_number;
    private String phone_code;
    private int sms_type;

    public Phone() {
        sms_type = 0;
    }

    public Boolean getPhone_registered() {
        return phone_registered;
    }

    public void setPhone_registered(Boolean phone_registered) {
        this.phone_registered = phone_registered;
    }

    public String getPhone_code_hash() {
        return phone_code_hash;
    }

    public void setPhone_code_hash(String phone_code_hash) {
        this.phone_code_hash = phone_code_hash;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPhone_code() {
        return phone_code;
    }

    public void setPhone_code(String phone_code) {
        this.phone_code = phone_code;
    }

    public int getSms_type() {
        return sms_type;
    }

    public void setSms_type(int sms_type) {
        this.sms_type = sms_type;
    }
}
