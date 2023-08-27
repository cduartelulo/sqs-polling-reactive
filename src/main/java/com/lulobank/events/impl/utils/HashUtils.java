package com.lulobank.events.impl.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils for hashing operations
 * @author Carlos Duarte
 */
public class HashUtils {

    public static String calculateSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
