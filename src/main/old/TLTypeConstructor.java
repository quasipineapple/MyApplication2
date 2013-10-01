/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 21.07.13
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
package com.tl;

public class TLTypeConstructor {
    private final int i;
    private final String s;
    private TLMapPair pq;

    public TLTypeConstructor(int i, String s) {


        this.i = i;
        this.s = s;
    }

    public void addField(TLMapPair pq) {
        this.pq = pq;
    }

    public Integer getId() {


        return null;
    }


    public String getName() {

        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
