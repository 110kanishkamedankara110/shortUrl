package com.phoenix.urlshort.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


public class SHA256Helper {
    public static String generateSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash=digest.digest(
                input.getBytes(StandardCharsets.UTF_8)
        );
        return HexFormat.of().formatHex(hash);
    }
}
