package com.lulobank.events.api.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utils for hashing operations
 * @author Carlos Duarte
 */
public class HashUtils {

    private static final String SHA_256_ALGORITHM = "SHA-256";

    private static final int HEX_LENGTH = 1;

    private HashUtils() {
    }

    public static String calculateSHA256Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
        byte[] encodedHash = digest.digest(input.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == HEX_LENGTH) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
