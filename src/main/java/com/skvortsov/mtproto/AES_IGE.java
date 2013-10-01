package com.skvortsov.mtproto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES_IGE {
    private byte[] key;
    private byte[] iv_1, iv_2;

    public AES_IGE(byte[] key, byte[] iv_1, byte[] iv_2) {
        this.key = key;

        this.iv_1 = iv_1;
        this.iv_2 = iv_2;
    }

    public byte[] encrypt(byte[] data) {
        return this.crypt(data, true);
    }
    public byte[] decrypt(byte[] data) {
        return this.crypt(data, false);
    }

    private byte[] crypt(byte[] data, boolean isEncrypt) {
        byte[] result = new byte[data.length];

        int dataOffset = 0;

        byte[] prevTop;
        byte[] prevBottom;

        if (isEncrypt) {
            prevTop = this.iv_1;
            prevBottom = this.iv_2;
        } else {
            prevTop = this.iv_2;
            prevBottom = this.iv_1;
        }

        byte[] current = new byte[16];

        while (dataOffset < data.length) {
            System.arraycopy(data, dataOffset, current, 0, 16);

            byte[] newBottom = current.clone();

            AES_IGE.xor(current, prevTop);

            current = this.pureAESCrypt(current, isEncrypt);

            AES_IGE.xor(current, prevBottom);

            byte[] newTop = current.clone();

            System.arraycopy(current, 0, result, dataOffset, 16);

            prevTop = newTop;
            prevBottom = newBottom;

            dataOffset += 16;
        }

        return result;
    }

    private static void xor(byte[] that, byte[] withThat) {
        for (int i = 0; i < that.length; i++) {
            that[i] ^= withThat[i];
        }
    }

    private byte[] pureAESCrypt(byte[] data, boolean isEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NOPADDING");

            SecretKey secretKey = new SecretKeySpec(this.key, "AES");

            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);

            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(5);
        }

        return null;
    }
}
