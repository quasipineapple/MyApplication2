package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/8/13.
 */
public class User {


    private String first_name;
    private String last_name;
    private Phone phone;


    public User(String first, String last) {
        this.first_name = first;
        this.last_name = last;

    }

    public User() {
        this.phone = new Phone();

    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getFirst_name() {
        return first_name;
    }


    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }


    public Boolean getPhone_registered() {
        return phone.getPhone_registered();
    }

    public void setPhone_registered(Boolean phone_registered) {
        phone.setPhone_registered(phone_registered);
    }

    public String getPhone_code_hash() {
        return phone.getPhone_code_hash();
    }

    public void setPhone_code_hash(String phone_code_hash) {
        phone.setPhone_code_hash(phone_code_hash);
    }

    public String getPhone_number() {
        return phone.getPhone_number();
    }

    public void setPhone_number(String phone_number) {
        phone.setPhone_number(phone_number);
    }

    public String getPhone_code() {
        return phone.getPhone_code();
    }

    public void setPhone_code(String phone_code) {
        phone.setPhone_code(phone_code);
    }

    public int getSms_type() {
        return phone.getSms_type();
    }

    public void setSms_type(int sms_type) {
        phone.setSms_type(sms_type);
    }
}
