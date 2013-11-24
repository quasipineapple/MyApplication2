package com.skvortsov.mtproto;

import com.skvortsov.mtproto.communication.PacketReader;
import com.skvortsov.mtproto.filter.ConstructorFilter;

import java.util.LinkedList;

/**
 * Created by skvortsov on 10/10/13.
 */
public class ConstructorCollector {

    private static final int MAX_CONSTRUCTORS = 65536;


    private LinkedList<Constructor> resultQueue;
    private PacketReader packetReader;
    private boolean cancelled = false;
    private ConstructorFilter constructorFilter;

    public ConstructorCollector(PacketReader packetReader, ConstructorFilter constructorFilter) {
        this.constructorFilter = constructorFilter;
        this.resultQueue = new LinkedList<Constructor>();
        this.packetReader = packetReader;
    }


    public synchronized void processConstructor(Constructor constructor) {

        if(constructor == null){
            return;
        }

        if(constructorFilter == null || constructorFilter.accept(constructor)){

            if(resultQueue.size() == MAX_CONSTRUCTORS){
                resultQueue.removeLast();
            }

            resultQueue.addFirst(constructor);

            notifyAll();

        }

    }

    public synchronized Constructor nextResult(long timeout) {

        // Wait up to the specified amount of time for a result.
        if (resultQueue.isEmpty()) {
            long waitTime = timeout;
            long start = System.currentTimeMillis();
            try {
                // Keep waiting until the specified amount of time has elapsed, or
                // a packet is available to return.
                while (resultQueue.isEmpty()) {
                    if (waitTime <= 0) {
                        break;
                    }
                    wait(waitTime);
                    long now = System.currentTimeMillis();
                    waitTime -= (now - start);
                    start = now;
                }
            }
            catch (InterruptedException ie) {
                // Ignore.
            }
            // Still haven't found a result, so return null.
            if (resultQueue.isEmpty()) {
                return null;
            }
            // Return the packet that was found.
            else {
                return resultQueue.removeLast();
            }
        }
        // There's already a packet waiting, so return it.
        else {
            return resultQueue.removeLast();
        }

    }

    public void cancel() {

        // If the packet collector has already been cancelled, do nothing.
        if (!cancelled) {
            cancelled = true;
            packetReader.cancelPacketCollector(this);
        }
    }
}
