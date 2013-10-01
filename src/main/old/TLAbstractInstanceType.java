package com.tl;



import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 21.07.13
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class TLAbstractInstanceType {
    public int calcSize(Object data) {


        return 0;
    }

    public abstract void serialize(Object data, ByteBuffer buffer) throws NotImplementedYet;

    public abstract TLStringInstanceType box();

    public abstract Object deserialize(ByteBuffer buffer);
}
