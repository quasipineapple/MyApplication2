package com.skvortsov.mtproto;

import com.skvortsov.mtproto.communication.PacketReader;
import com.skvortsov.mtproto.interfaces.ConstructorFilter;

import java.util.LinkedList;

/**
 * Created by skvortsov on 10/10/13.
 */
public class ConstructorCollector {

    private static final int MAX_CONSTRUCTORS = 65536;

    private ConstructorFilter packetFilter;
    private LinkedList<Constructor> resultQueue;
    private PacketReader packetReader;
    private boolean cancelled = false;

    public ConstructorCollector(ConstructorFilter packetFilter, PacketReader packetReader) {
        this.packetFilter = packetFilter;
        this.resultQueue = new LinkedList<Constructor>();
        this.packetReader = packetReader;
    }

    public void processConstructor(Constructor constructor) {

        if(constructor == null){
            return;
        }



    }
}
