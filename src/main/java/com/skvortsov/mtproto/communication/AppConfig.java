package com.skvortsov.mtproto.communication;

/**
 * Created by skvortsov on 10/1/13.
 */
public final class AppConfig {

    private static final String MTP_VERSION = "1.0";

    private static int packetReplyTimeout = 15000;

    private AppConfig() {
    }

    public static String getMtpVersion() {
        return MTP_VERSION;
    }

    public static int getPacketReplyTimeout() {
        return packetReplyTimeout;
    }

    public static void setPacketReplyTimeout(int packetReplyTimeout) {
        AppConfig.packetReplyTimeout = packetReplyTimeout;
    }
}
