package com.tl;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 21.07.13
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
public class PayloadPacket {

    private static ByteBuffer buffer;

    public String Req_pq;
    public byte[] nonce;


    public PayloadPacket(int i) {

        buffer = ByteBuffer.allocate(i);

    }

    public byte[] Length()
    {

        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(buffer.limit()).array();
    }

    public void setAuthKeyId(byte[] bytes) {

            buffer.put(bytes);
    }

    public void setMessageId() {

        long unixTime = (System.currentTimeMillis() / 1000L) << 32;
        buffer.putLong(unixTime);
    }

    public void setPayload(byte[] serialized) {
        buffer.put(serialized );
        //To change body of created methods use File | Settings | File Templates.
    }
}
