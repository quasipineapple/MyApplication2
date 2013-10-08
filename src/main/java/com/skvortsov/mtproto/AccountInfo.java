package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/8/13.
 */
public class AccountInfo {

    private User user;

    public AccountInfo(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername(){

        return user.getUsername();

    }
}
