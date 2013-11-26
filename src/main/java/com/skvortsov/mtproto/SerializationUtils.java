package com.skvortsov.mtproto;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by сергей on 02.08.13.
 */
public class SerializationUtils {

    private static final String TAG = "SerializationUtils";

    public static byte[] serialize(Constructor result) {
        byte[] serialized = new byte[calcSize(result)];
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        _serialize(result, buffer);

        return serialized;
    }

    private static void _serialize(Constructor result, ByteBuffer buffer) {

        buffer.putInt(Integer.valueOf(result.getId()));

        if (result.getParams().size() > 0){
            for (Param p : result.getParams()){
                _serialize(p, buffer);
            }
        }
    }

    private static void _serialize(Param param, ByteBuffer buffer) {

        if (param.getData() == null) throw new RuntimeException("Serialization. Cannot serialize. No data.");

        if (param.getType().equals("int")) {
            buffer.putInt((Integer) param.getData());

        } else if (param.getType().equals("long")) {
            buffer.putLong((Long) param.getData());

        } else if (param.getType().equals("double")) {
            buffer.putDouble((Double)param.getData());

        } else if (param.getType().equals("int128")){
            byte[] b = (byte[]) param.getData();
            buffer.put(b);

        } else if (param.getType().equals("int256")){
            byte[] b = (byte[]) param.getData();
            buffer.put(b);

        } else if (param.getType().equals("string")) {

            _serializeString(param.getData(), buffer);

        } else if (param.getType().contains("Vector")){

            _serializeV(param, buffer);

        } else {
            if (param.getData() instanceof Constructor) {
                if (((Constructor)param.getData()).getType().equals(param.getType())) {
                    _serialize((Constructor) param.getData(), buffer);
                } else throw new RuntimeException("Serialization. Constructor type: " + ((Constructor)param.getData()).getType() + " not equals parameter type: " + param.getType());
            } else throw new RuntimeException("Serialization." + param.getType() + " " + param.getName() + " not instance of class Constructor");
        }
    }

    private static void _serializeString(Object data, ByteBuffer buffer) {

        int bytesLength;

        if (data instanceof String) {
            bytesLength = UTF8.calcSize((String) data);
        } else {
            bytesLength = ((byte[]) data).length;
        }
        int length = bytesLength + (bytesLength <= 253 ? 1 : 4);

        if (bytesLength <= 253) {
            buffer.put((byte) bytesLength);
        } else {
            buffer.put((byte) 254);
            byte q = (byte) ((bytesLength) & 0xff),
                    w = (byte) ((bytesLength >>> 8) & 0xff),
                    e = (byte) ((bytesLength >>> 16) & 0xff);

            buffer.put(q);
            buffer.put(w);
            buffer.put(e);
        }

        if (data instanceof String) {
            UTF8.encode((String) data, buffer);
        } else {
            buffer.put((byte[]) data);
        }

        buffer.position(buffer.position() + (4 - length % 4) % 4);

    }

    private static void _serializeV(Param param, ByteBuffer buffer) {

        /*Для сериализации необходимо записать номер
        конструктора 0x1cb5c415:int, затем количество элементов вектора - #:int,
        после этого необходимо последовательно записывать # самих элементов типа t,
        который был неявно передан в конструктор.
         */
        buffer.putInt(481674261);

        if(param.getData() instanceof Integer[]){
            Integer[] data = (Integer[])param.getData();
            buffer.putInt(data.length);
            for (Integer aData : data) {
                buffer.putInt(aData);
            }
        } else if(param.getData() instanceof Long[]){
            Long[] data = (Long[])param.getData();
            buffer.putInt(data.length);
            for (Long aData : data) {
                buffer.putLong(aData);
            }
        } else if(param.getData() instanceof Double[]){
            Double[] data = (Double[])param.getData();
            buffer.putInt(data.length);
            for (Double aData : data) {
                buffer.putDouble(aData);
            }
        } else if(param.getType().contains("int128") || param.getType().contains("int256")) {
            byte[] data = (byte[])param.getData();
            buffer.putInt(data.length);
            buffer.put(data);
        } else if(param.getType().contains("string")) {
            String[] data = (String[])param.getData();
            for (String s : data){
                _serializeString(s, buffer);
            }
        } else if(param.getData() instanceof Constructor[]){
            Constructor[] data = (Constructor[])param.getData();
            buffer.putInt(data.length);
            for (Constructor aData : data) {
                _serialize(aData, buffer);
            }
        } else {
            throw new RuntimeException("_serializeV. Cannot serialize.");
        }

    }

    public static int calcSize(Constructor c) {
        int i = 4;
        if (c.getParams().size() > 0){
            for (Param p : c.getParams()){
                i += _calcSize(p);
            }
        }
        return i;
    }

    private static int _calcSize(Param p) {

        if (p.getData() == null) throw new RuntimeException("_calcSize. Cannot _calcSize. No data.");

        if (p.getType().equals("int")) {
            return 4;

        } else if (p.getType().equals("long") || p.getType().equals("double")) {
            return 8;

        } else if (p.getType().equals("int128")) {
            return 16;

        } else if (p.getType().equals("int256")) {
            return 32;

        } else if (p.getType().equals("string")) {
            int bytesLength;

            if (p.getData() instanceof String) {
                bytesLength = UTF8.calcSize((String) p.getData());
            } else {
                bytesLength = ((byte[]) p.getData()).length;
            }

            //byte[] bytes = String.valueOf(p.getData()).getBytes();

            int length = (bytesLength <= 253 ? 1 : 4) + bytesLength;
            return length + (4 - length % 4) % 4;

        } else if (p.getType().contains("Vector")){

            return _calcSizeV(p.getData());

        } else {
            if (p.getData() instanceof Constructor) {
                if (((Constructor)p.getData()).getType().equals(p.getType())) {
                    return calcSize((Constructor) p.getData());

                } else throw new RuntimeException("_calcSize. Constructor type: " + ((Constructor)p.getData()).getType() + " not equals parameter type: " + p.getType());
            } else throw new RuntimeException("_calcSize." + p.getType() + " " + p.getName() + " not instance of class Constructor");
        }
    }

    private static int _calcSizeV(Object data) {

        int total = 4 + 4;
        int size;

        if(data instanceof Integer[]){
            Integer[] d = (Integer[])data;
            size = 4 * d.length;
        } else if(data instanceof Long[]){
            Long[] d = (Long[])data;
            size =  8 * d.length;
        } else if(data instanceof Double[]){
            Double[] d = (Double[])data;
            size =  16 * d.length;
        } else if(data instanceof byte[]) {
            byte[] d = (byte[])data;
            size =  d.length;
        } else if(data instanceof String[]) {
            int t = 0;
            String[] d = (String[])data;
            for(String s : d){
                byte[] bytes = s.getBytes();
                int length = (bytes.length <= 253 ? 1 : 4) + bytes.length;
                t = t + (length + (4 - length % 4) % 4);
            }
            size = t;
        } else if(data instanceof Constructor[]){
            int t = 0;
            Constructor[] d = (Constructor[])data;
            for (Constructor c : d) {
                t += calcSize(c);
            }
            size =  t;
        } else {
            throw new RuntimeException("_calcSizeV. Cannot calc size.");
        }

        return total + size;
    }

    public static Constructor deserialize(byte[] message_data) throws CloneNotSupportedException {

        ByteBuffer bb = ByteBuffer.wrap(message_data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Constructor c = BookManager.getBook().getConstructorById(String.valueOf(bb.getInt())).clone();

        if (c.getParams().size() > 0){
            for (Param p : c.getParams()){
                _deserialize(p,bb);
            }
        }
        return c;
    }

    private static void _deserialize(Param p, ByteBuffer bb) throws CloneNotSupportedException {

        //Log.i(TAG, p.getType().substring(0, p.getType().length()));
        //Log.i(TAG, "Vector".matches()substring(0, p.getType().length()));

        if(p.getType().equals("int")){
            p.setData(bb.getInt());
            //bb.position(4 + bb.position());
        }else if(p.getType().equals("long")){
            p.setData(bb.getLong());

        }else if(p.getType().equals("double")){
            p.setData(bb.getDouble());

        }else if(p.getType().equals("int128")){
            byte[] bytes = new byte[16];
            bb.get(bytes);
            p.setData(bytes);

        }else if(p.getType().equals("int256")){
            byte[] bytes = new byte[32];
            bb.get(bytes);
            p.setData(bytes);

        }else if(p.getType().equals("string")){

            int first = bb.get() & 0xff;
            int length = 0;
            int totalLength;

            if (first <= 253) {
                length = first;
                totalLength = length + 1;
            } else {
                length = ((bb.get() & 0xff))
                        + ((bb.get() & 0xff) << 8)
                        + ((bb.get() & 0xff) << 16);
                totalLength = length + 4;
            }

            byte[] bytes = new byte[length];
            bb.get(bytes);
            bb.position(bb.position() + (4 - totalLength % 4) % 4);
            p.setData(bytes);

        }else if(p.getType().contains("Vector")){
            Object[] values = null;
            values = _deserializeV(p, bb);
            p.setData(values);
        }else {
            Constructor c = BookManager.getBook().getConstructorById(String.valueOf(bb.getInt())).clone();
            if (c.getParams().size() > 0){
                for (Param pp : c.getParams()){
                    _deserialize(pp,bb);
                }
            }

            p.setData(c);

            /*if (p.getData() instanceof Constructor) {
                if (((Constructor)param.getData()).getType().equals(param.getType())) {
                    _serialize((Constructor) param.getData(), buffer);
                } else throw new RuntimeException("Serialization. Constructor type: " + ((Constructor)param.getData()).getType() + " not equals parameter type: " + param.getType());
            } else throw new RuntimeException("Serialization." + param.getType() + " " + param.getName() + " not instance of class Constructor");
            */
        }

    }

    private static Object[] _deserializeV(Param p, ByteBuffer bb) throws CloneNotSupportedException {
        //String type = p.getType().substring(7, p.getType().length() - 1);
        // проверка на msg_container
        if(!p.getType().contains("message")){
            //здесь нужно проверить что получен конструктор универсального вектора
            int ii = bb.getInt();
            if(ii != 481674261){
                throw new RuntimeException("Error! Vector id != 481674261. Не получен ожидаемый конструктор универсального вектора! Vector id = " + ii);
            }else{
                //Log.i(TAG, "Vector id = " + ii);
            }
        }
        //кол-во элементов в векторе
        int count = bb.getInt();
        Object[] values = null;

        if(p.getType().contains("int")){
            values = new Integer[count];
        }else if(p.getType().contains("long")){
            Log.i(TAG, "Vector type " + p.getType());
            values = new Long[count];
        }else if(p.getType().contains("double")){
            values = new Double[count];
        }else {
            //вектор состоит из конструкторов
            values = new Constructor[count];
        }

        for (int i = 0; i < count; i++) {

            if(p.getType().contains("int")){
                values[i] = bb.getInt();
            }else if(p.getType().contains("long")){
                values[i] = bb.getLong();
            }else if(p.getType().contains("double")){
                values[i] = bb.getDouble();
            }else {
                //вектор состоит из конструкторов
                Constructor cc = BookManager.getBook().getConstructorById(String.valueOf(bb.getInt())).clone();
                values[i] = cc;

                if (cc.getParams().size() > 0){
                    for (Param pp : cc.getParams()){

                        _deserialize(pp,bb);

                    }
                }

            }
        }

        return values;
    }


    public static byte[] serialize(Method result) {
        byte[] serialized = new byte[calcSize(result)];
        System.out.println(TAG + "serialized.length: " + serialized.length);
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        _serialize(result, buffer);

        return serialized;
    }

    private static void _serialize(Method result, ByteBuffer buffer) {
        buffer.putInt(Integer.valueOf(result.getId()));

        if (result.getParams().size() > 0){
            for (Param p : result.getParams()){
                _serialize(p, buffer);
            }
        }
    }

    public static int calcSize(Method m) {
        int i = 4;
        if (m.getParams().size() > 0){
            for (Param p : m.getParams()){
                i += _calcSize(p);
            }
        }
        return i;
    }
}
