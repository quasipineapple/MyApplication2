/**
 * Created with IntelliJ IDEA.
 * User: сергей
 * Date: 20.07.13
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
package com.tl;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.Checksum;




public class Packet {



    private ByteBuffer _buffer;

    private byte[] _message_length;
    private byte[] _constructor_name;
    private byte[] _nonce;
    private byte[] _auth_key_id;
    private byte[] _message_id;
    private byte[] _length;
    private byte[] _num;
    private byte[] _p_q_inner_data;
    private byte[] _pq;
    private byte[] _p;
    private byte[] _q;
    private byte[] _server_nonce;
    private byte[] _encrypted_data;
    private byte[] _public_key_fingerprint;
    private byte[] _new_nonce;
    private byte[] _client_DH_inner_data;


    public Packet(int length)
    {
        _buffer = ByteBuffer.allocate(length);
        _buffer.put(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(length).array());
          this._length = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(length).array();
    }

    public Packet(int length, Boolean empty)
    {
        _buffer = ByteBuffer.allocate(length);

    }

    public Packet(ByteBuffer buf)
    {
        //_buffer = ByteBuffer.allocate(buf.limit());
        _buffer = ByteBuffer.wrap(buf.array());


    }

    public Packet(byte[] buf)
    {
        //_buffer = ByteBuffer.allocate(buf.limit());
        _buffer = ByteBuffer.wrap(buf);


    }

    public void set_message_length(byte[] _message_length) {
        _buffer.put(_message_length);
        this._message_length = _message_length;
    }

    public byte[] get_message_length() {
        return _message_length;
    }

    public void set_constructor_name(byte[] _constructor_name) {
        _buffer.put(_constructor_name);
        this._constructor_name = _constructor_name;
    }

    public byte[] get_constructor_name() {
        return _constructor_name;
    }

    public void set(byte[] any)
    {
         _buffer.put(any);
    }


    public void set_nonce(byte[] _nonce) {
        _buffer.put(_nonce);
        this._nonce = _nonce;
    }

    public byte[] get_nonce() {
        return _nonce;
    }

    public void set_auth_key_id(byte[] _auth_key_id) {
        _buffer.put(_auth_key_id);
        this._auth_key_id = _auth_key_id;

    }

    public byte[] get_auth_key_id() {
        return _auth_key_id;
    }

    public void set_message_id(byte[] _message_id) {
        _buffer.put(_message_id);
        this._message_id = _message_id;
    }

    public byte[] get_message_id() {
        return _message_id;
    }

    public void decompose_resPQ(ByteBuffer buffer)
    {

    }

    public void preview(String s) {
        //To change body of created methods use File | Settings | File Templates.
        System.out.println(s + Helpers.bytesToHex(_buffer.array()));
    }

    public void send() throws IOException {

    }

    public void set_length(byte[] _length) {

    }

    public byte[] get_length() {
        return _length;
    }

    public void setBuffer(ByteBuffer buffer) {
        this._buffer = buffer;
    }

    public void setBuffer(byte[] buffer) {
        this._buffer = ByteBuffer.wrap(buffer);
    }


    public void set_num(byte[] _num) {
        _buffer.put(_num);
        this._num = _num;
    }

    public byte[] get_num() {
        return _num;
    }

    public void set_crc32() {
        Checksum checksum = new CRC32();
        checksum.update(this._buffer.array(),this._buffer.arrayOffset(),this._buffer.array().length - 4);
        _buffer.put(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) checksum.getValue()).array());

    }

    public ByteBuffer get_buffer() {
        return _buffer;
    }

    public byte[] get_as_bytes_BE(int i, int i1) {


        byte[] bytes = new byte[i1];

        _buffer.position(i);
        _buffer.get(bytes, 0, i1);



        return bytes;
    }

    public void set_p_q_inner_data(byte[] _p_q_inner_data) {
        _buffer.put(_p_q_inner_data);
        this._p_q_inner_data = _p_q_inner_data;
    }

    public byte[] get_p_q_inner_data() {
        return _p_q_inner_data;
    }

    public void set_pq(byte[] _pq) {
        _buffer.put(_pq);
        this._pq = _pq;
    }

    public byte[] get_pq() {
        return _pq;
    }


    public byte[] get_p_as_byte_array() {
        return _p;
    }




    public void set_p(BigInteger biP) {

        ByteBuffer p = ByteBuffer.allocate(8);
        p.put(new byte[]{4});
        p.put(biP.toByteArray());
        p.put(new byte[] {0, 0, 0});

        _buffer.put(p.array());

        this._p = p.array();
    }

    public byte[] get_q() {
        return _q;
    }

    public void set_q(byte[] _q) {
        _buffer.put(_q);
        this._p = _q;
    }

    public void set_q(BigInteger biQ) {

        ByteBuffer q = ByteBuffer.allocate(8);
        q.put(new byte[] {4} );
        q.put(biQ.toByteArray());
        q.put(new byte[]{0, 0, 0});

        _buffer.put(q.array());

        this._q = q.array();
        //To change body of created methods use File | Settings | File Templates.
    }

    public byte[] get_server_nonce() {


        return _server_nonce;
    }

    public void set_server_nonce(byte[] _server_nonce) {

        _buffer.put(_server_nonce);
        this._server_nonce = _server_nonce;
    }


    public void set_p(byte[] p_as_byte_array) {

        _buffer.put(p_as_byte_array);
        this._p = p_as_byte_array;
        //To change body of created methods use File | Settings | File Templates.
    }

    public byte[] get_q_as_byte_array() {
        return _q;
    }

    public void set_encrypted_data(byte[] _encrypted_data) {
        _buffer.put(_encrypted_data);
        this._encrypted_data = _encrypted_data;
    }

    public void set_public_key_fingerprint(byte[] _public_key_fingerprint) {
        _buffer.put(_public_key_fingerprint);
        this._public_key_fingerprint = _public_key_fingerprint;
    }

    public void set_new_nonce(byte[] _new_nonce) {
        _buffer.put(_new_nonce);
        this._new_nonce = _new_nonce;
    }

    public void set_client_DH_inner_data(byte[] _client_DH_inner_data) {
        _buffer.put(_client_DH_inner_data);
        this._client_DH_inner_data = _client_DH_inner_data;
    }
}
