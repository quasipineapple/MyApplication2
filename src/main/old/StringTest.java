package com.tl;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: skvortsov
 * Date: 26.07.13
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
public class StringTest {

    public static void main(String args[]) throws UnsupportedEncodingException {

        //byte[] b1 = Helpers.hexStringToByteArray("11494E50");
        //byte[] b1 = Helpers.hexStringToByteArray("11494E50");
        //byte[] b2 = Helpers.hexStringToByteArray("55545F46");



        byte[] b1 = Helpers.hexStringToByteArray("39300000");
        byte[] b2 = Helpers.hexStringToByteArray("12D0A1D0B5D180D0B3D0B5D0B920202020202000");
        byte[] b3 = Helpers.hexStringToByteArray("0d2b3338303933313233343536370000");
        byte[] b4 = Helpers.hexStringToByteArray("12000000");
        byte[] b5 = Helpers.hexStringToByteArray("0CD09CD0BED181D0BAD0B2D0B0000000");
        /*byte[] bytes = ByteBuffer.allocate(4).putInt(1695609641).array();

        BigInteger bi1 = new BigInteger(b1);
        BigInteger bi2 = new BigInteger(b2);
        BigInteger bi3 = new BigInteger(b3);
        BigInteger bi4 = new BigInteger(b4);
        BigInteger bi5 = new BigInteger(b5);

        int i1 = bi1.intValue();
        int i2 = bi2.intValue();
        int i3 = bi3.intValue();
        int i4 = bi4.intValue();
        int i5 = bi5.intValue();

        */
        //+ bi2.toString() + bi3.toString() + bi4.toString() + bi5.toString()
        String s1 = new String(b1, "UTF-8");
        String s2 = new String(b2, "UTF-8");
        String s3 = new String(b3, "UTF-8");
        String s4 = new String(b4, "UTF-8");
        String s5 = new String(b5, "UTF-8");
        System.out.println("s1:" + s1);
        System.out.println("s2:" + s2);
        System.out.println("s3:" + s3);
        System.out.println("s4:" + s4);
        System.out.println("s5:" + s5);

        //String name = new String("Москва");      //15

        //byte[] name_as_byte = new byte[] {};
        //name_as_byte = ByteBuffer.allocate(1).put((byte) name.length()).array();

        //name_as_byte = Helpers.concat(name_as_byte, name.getBytes(Charset.forName("UTF-8")));

        //System.out.println("name as byte: " + Helpers.bytesToHex(name_as_byte));
        //System.out.println("name as byte length: " + Helpers.bytesToHex(name_as_byte).length());
        /*
        int[] num = new int[raw.length()];

        for (int i = 0; i < raw.length(); i++){
            num[i] = raw.charAt(i) - '0';
        }

        for (int i : num) {
            System.out.println(i);
        }



        String name = new String("+79056624155");      //15
        System.out.println("name length: " + name.length());

        byte[] name_as_byte;

        name_as_byte = ByteBuffer.allocate(1).put((byte) name.length()).array();

        name_as_byte = Helpers.concat(name_as_byte, name.getBytes(Charset.forName("UTF-8")));

        System.out.println("name as byte: " + Helpers.bytesToHex(name_as_byte));
        System.out.println("name as byte length: " + name_as_byte.length);

        if (name_as_byte.length % 4 != 0)
        {
            System.out.println("WARNING name_as_byte.length % 4 != 0 !!!!");
            int i = name_as_byte.length + 1;

            while(i % 4 != 0){
                i = i + 1;
            }

            System.out.println("name_as_byte length " + name_as_byte.length + " increase to " + i);
            byte[] tmp = new byte[i];
            System.arraycopy(name_as_byte, 0, tmp, 0, name_as_byte.length);
            name_as_byte = tmp;
            System.out.println("new name_as_byte " + Helpers.bytesToHex(name_as_byte));
            System.out.println("new name_as_byte length " + name_as_byte.length);


        }


        */
    }


    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }



}
