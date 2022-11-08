package com.jloysch.smackandrack;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static byte[] encrypt(byte[] data, SecretKey encryptionKey) throws Exception {
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, iv);
        byte[] encryptData = cipher.doFinal(data);

        return encryptData;
    }

    public static byte[] decrypt(byte[] tmp, SecretKey encryptionKey) throws Exception {
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
        SecretKeySpec spec = new SecretKeySpec(encryptionKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, spec, iv);

        //System.out.println(tmp.length);
        return cipher.doFinal(tmp);

    }
}


