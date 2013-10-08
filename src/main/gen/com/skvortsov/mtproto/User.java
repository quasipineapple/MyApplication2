package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/8/13.
 */
public class User {


    private String username;


    public User(String username) {
        this.username = username;
    }

    public User() {

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }


}
