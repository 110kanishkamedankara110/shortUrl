package com.phoenix.urlshort.util;

import java.security.SecureRandom;

public final class Base62Util {

    private static final String CHARACTERS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static final int KEY_LENGTH = 7;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateKey() {
        StringBuilder key = new StringBuilder(KEY_LENGTH);

        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            key.append(CHARACTERS.charAt(index));
        }

        return key.toString();
    }
}