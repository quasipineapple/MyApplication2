package com.skvortsov.mtproto.interfaces;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IAppManager {

    public String help_getConfig() throws CloneNotSupportedException, NoSuchAlgorithmException, IOException;

    public String auth_sendCode(String tel) throws IOException, CloneNotSupportedException, NoSuchAlgorithmException;
    public String auth_signIn(String tel) throws IOException, CloneNotSupportedException, NoSuchAlgorithmException;


	public String signUpUser(String usernameText, String passwordText, String email);
    public boolean isNetworkConnected();

}
