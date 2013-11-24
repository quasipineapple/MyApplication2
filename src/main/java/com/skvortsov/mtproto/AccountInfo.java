package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/8/13.
 */
public class AccountInfo {

    private User user;

    public AccountInfo() {
        this.user = new User();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername(){

        return user.getFirst_name();

    }

    public void setPhone_registered(Boolean phone_registered) {
        user.setPhone_registered(phone_registered);
    }

    public Boolean getPhone_registered() {
        return user.getPhone_registered();
    }

    public void setFirst_name(String first_name) {
        user.setFirst_name(first_name);
    }

    public void setPhone(Phone phone) {
        user.setPhone(phone);
    }

    public void setPhone_code(String phone_code) {
        user.setPhone_code(phone_code);
    }

    public void setSms_type(int sms_type) {
        user.setSms_type(sms_type);
    }

    public void setPhone_number(String phone_number) {
        user.setPhone_number(phone_number);
    }

    public String getPhone_code_hash() {
        return user.getPhone_code_hash();
    }

    public String getPhone_code() {
        return user.getPhone_code();
    }

    public void setLast_name(String last_name) {
        user.setLast_name(last_name);
    }

    public void setPhone_code_hash(String phone_code_hash) {
        user.setPhone_code_hash(phone_code_hash);
    }

    public String getLast_name() {
        return user.getLast_name();
    }

    public Phone getPhone() {
        return user.getPhone();
    }

    public int getSms_type() {
        return user.getSms_type();
    }

    public String getFirst_name() {
        return user.getFirst_name();
    }

    public String getPhone_number() {
        return user.getPhone_number();
    }
}
