package com.gnc.task.application.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class CustomEncoderEncrypt {

    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'S', 'u', 'm', 'm', 'a', 'M', 't', 'a', 's', 'k', 'U', 'I', 'S',
            'K', 'e', 'y'};

    /**
     * Encrypt a string with AES algorithm.
     *
     * @param data is a string
     * @return the encrypted string
     */
    public static String encrypt(String data) {
        try {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;

    }

    /**
     * Decrypt a string with AES algorithm.
     *
     * @param cipherText is a string
     * @return the decrypted string
     */
    public static String decrypt(String cipherText) {
        Key key = null;
        byte[] plainText = new byte[0];
        try {
            key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key);
            plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(plainText);
    }

    /**
     * Generate a new encryption key.
     */
    private static Key generateKey() throws Exception {
        return new SecretKeySpec(keyValue, ALGO);
    }
}
