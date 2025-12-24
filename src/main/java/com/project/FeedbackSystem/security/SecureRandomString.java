package com.example.FeedbackSystem.security;

import java.security.SecureRandom;
import java.util.Base64;

// This class generate token for jwt refresh
public class SecureRandomString {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateToken(int byteLength){  // byteLength give the refreshToken length
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // will return a length of randomly generated string
    }
}
