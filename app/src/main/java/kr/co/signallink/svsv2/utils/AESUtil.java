package kr.co.signallink.svsv2.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

/**
 * CryptographyWrapper class
 */
public class AESUtil {

    private final String encryptionKey;
    private byte[] keyBytes;
    private Cipher cipher;
    private SecretKeySpec key;

    public AESUtil(String encryptionKey) {
        this.encryptionKey = encryptionKey;
        keyBytes = encryptionKey.getBytes();
        key = new SecretKeySpec(keyBytes, "AES");
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt Method
     *
     * @param plainText text to be encrypted
     * @return encryptedText
     */
    public String encrypt(String plainText) {
        String encryptedText = plainText;
        if (!plainText.trim().equalsIgnoreCase("") || encryptedText.length() != 0) {
            byte[] cipherText = encryptData(plainText.getBytes());
            encryptedText = Base64.encodeToString(cipherText, Base64.DEFAULT);
        }
        return encryptedText;
    }

    /**
     * Decrypt method
     *
     * @param encryptedText encrypted text
     * @return plain text
     */
    public String decrypt(String encryptedText) {
        String decryptedText = encryptedText;
        try {
            if (!encryptedText.equals("") || !encryptedText.equals("0")) {
                byte[] encryptedTextByte = Base64.decode(encryptedText, Base64.DEFAULT);
                decryptedText = decryptData(encryptedTextByte);
                Log.d(AESUtil.class.getSimpleName(), decryptedText);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return decryptedText;
    }

    /**
     * Method used to encryptiong
     * return value is without Base64 encoding.
     *
     * @param plainText
     * @return encrypted Value
     */
    private byte[] encryptData(byte[] plainText) {
        byte[] encryptedValue = null;
        try {
            // encryptData pass
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] input = plainText;
            byte[] cipherText = new byte[cipher.getOutputSize(input.length)];

            int ctLength = cipher.update(input, 0, input.length, cipherText, 0);

            ctLength += cipher.doFinal(cipherText, ctLength);
            encryptedValue = cipherText;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (ShortBufferException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return encryptedValue;
    }

    /**
     * Private method to decrypt data
     *
     * @param cipherText base64 decoded value
     * @return decryptedValue
     */
    private String decryptData(byte[] cipherText) {
        String decryptedValue = "";
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            int ctLength = cipherText.length;
            byte[] plainText = new byte[cipher.getOutputSize(ctLength)];

            int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);

            ptLength += cipher.doFinal(plainText, ptLength);
            decryptedValue = new String(plainText, 0, ptLength, "UTF-8");
            return decryptedValue;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (ShortBufferException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decryptedValue;
    }

}
