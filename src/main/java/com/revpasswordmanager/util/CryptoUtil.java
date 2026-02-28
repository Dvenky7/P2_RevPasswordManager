package com.revpasswordmanager.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;
    private static final int KEY_SIZE = 32;

    /**
     * Encrypts the given raw text using AES-256 with a random IV.
     * The resulting string is Base64(IV + EncryptedContent).
     */
    public static String encrypt(String rawText, String secretKey) throws Exception {
        byte[] keyBytes = prepareKey(secretKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(rawText.getBytes());

        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts the given encrypted text (Base64'd IV + content).
     */
    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] encryptedBytes = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

        byte[] keyBytes = prepareKey(secretKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    private static byte[] prepareKey(String secretKey) {
        byte[] keyBytes = new byte[KEY_SIZE];
        byte[] secretBytes = secretKey.getBytes();
        int length = Math.min(secretBytes.length, KEY_SIZE);
        System.arraycopy(secretBytes, 0, keyBytes, 0, length);
        return keyBytes;
    }
}
