package com.skvortsov.mtproto.communication;

/**
 * Created by skvortsov on 9/24/13.
 */
public class ConnectionConfig {

    String host;
    int port;

    private boolean debuggerEnabled = MTPConnection.DEBUG_ENABLED;

    public ConnectionConfig(String host, int port) {
        init(host, port);
    }

    private void init(String host, int port){

        this.host = host;
        this.port = port;
    }

    public boolean isDebuggerEnabled() {
        return debuggerEnabled;
    }

    public void setDebuggerEnabled(boolean debuggerEnabled) {
        this.debuggerEnabled = debuggerEnabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
