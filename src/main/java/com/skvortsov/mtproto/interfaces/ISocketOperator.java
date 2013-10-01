package com.skvortsov.mtproto.interfaces;


import java.io.IOException;

public interface ISocketOperator {
	
	public String sendHttpRequest(String params);
    public byte[] sendHttpRequest(byte[] data) throws IOException;
    public byte[] sendPacket(byte[] data) throws IOException;

}
