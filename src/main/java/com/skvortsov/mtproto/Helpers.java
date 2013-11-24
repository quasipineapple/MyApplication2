package com.skvortsov.mtproto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static java.lang.Long.toHexString;

/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 21.07.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class Helpers {

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static byte[] random_bytes(int i){
        byte[] b = new byte[i];
        new Random().nextBytes(b);
        return b;
    }

    public static byte[] conv_from_LE_to_BE(byte[] array){
        for (int i = 0, j = array.length - 1; i < j; i++, j--)
        {
            byte b = array[i];
            array[i] = array[j];
            array[j] = b;
        }
        return array;
    }

    public static String CalculateCRC32ForString(String str)    {
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        long lngChecksum = checksum.getValue();
        String res = toHexString(lngChecksum);

        return res;
    }

    public static void CalculateCRC32ForByteArray (String str){
        CRC32 crc = new CRC32();
        crc.update(str.getBytes());
    }

    public static String CalculateLongCRC32ForString(String str){
        byte bytes[] = str.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes,0,bytes.length);
        return Long.toHexString(checksum.getValue());
    }

    public static int CalculateCRC32ForByteArray(ByteBuffer bb){
        Checksum checksum = new CRC32();
        checksum.update(bb.array(),bb.arrayOffset(),bb.array().length - 4);
        return (int) checksum.getValue();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String BytesToText(byte[] hex){


        return HexToString(bytesToHex(hex));
    }

    private static String HexToString(String hex){
        return HexStringConverter.getHexStringConverterInstance().hexToString(hex);
    }


    public static String StringToHex(String str){
        String res = "";
        try {
            res =  HexStringConverter.getHexStringConverterInstance().stringToHex(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return res;
    }

    public static byte[] serialize_string(String s) {

        byte bytes[] = s.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);

        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)checksum.getValue()).array();
    }

    public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }


    public static byte[] SHA1(byte[] convertme) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(convertme);
    }

    public static byte[] concat(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] LongTobyteArray(long l){
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(l).array();
    }


    public static byte[] substr(byte[] src, int from, int to){
        return Arrays.copyOfRange(src, from, to);
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String byteArrayToHex(byte[] array) {
        return byteArrayToHex(array, 0, array.length);
    }

    private static String byteArrayToHex(byte[] array, int offset, int length) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n---\n");

        boolean isFirst = true;
        int count = 0;
        for (int i = offset; i < offset + length; i++) {
            byte b = array[i];

            if (!isFirst) {
                if (count == 16) {
                    count = 0;
                    sb.append('\n');
                } else {
                    sb.append(' ');
                }
            } else {
                isFirst = false;
            }

            String a = Integer.toHexString(((int) b) & 0xff);
            if (a.length() == 1) a = '0' + a;
            sb.append(a);

            count++;
        }

        sb.append("\n---\n");

        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
