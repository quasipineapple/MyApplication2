package com.skvortsov.mtproto;

/**
 * Created by skvortsov on 10/8/13.
 */
public class Server {


    private String address;
    private int port;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
