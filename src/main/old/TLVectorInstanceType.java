//package by.muna.mtproto.tl.instances;
package com.tl;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

//import by.muna.exceptions.NotImplementedYet;
//import by.muna.mtproto.tl.TLTypeInstance;

public class TLVectorInstanceType extends TLAbstractInstanceType {
    private TLTypeInstance a;
    private boolean boxed;

    public TLVectorInstanceType(TLTypeInstance a) {
        this.a = a;
    }

    public TLVectorInstanceType(TLLongInstanceType tlLongInstanceType) {


    }

    @Override
    public int calcSize(Object data) {
        @SuppressWarnings("unchecked")
        int count = ((List<Object>) data).size();

        return super.calcSize(data) + 4 + count;
    }

    @Override
    public void serialize(Object data, ByteBuffer buffer) throws NotImplementedYet {
        throw new NotImplementedYet();
    }

    @Override
    public TLStringInstanceType box() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object deserialize(ByteBuffer buffer) {
        if (this.boxed) buffer.getInt();

        List<Object> result = new LinkedList<Object>();

        int count = buffer.getInt();
        for (int i = 0; i < count; i++) {
            result.add(this.a.deserialize(buffer));
        }

        return result;
    }

}



