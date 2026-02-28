package com.revpasswordmanager.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordGeneratorServiceImpl implements IPasswordGeneratorService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_+={}[]|;:,.<>?";
    private static final String SIMILAR = "0OIl1";

    private final SecureRandom random = new SecureRandom();

    public String generatePassword(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar) {
        StringBuilder charPool = new StringBuilder();
        if (useUpper)
            charPool.append(UPPER);
        if (useLower)
            charPool.append(LOWER);
        if (useDigits)
            charPool.append(DIGITS);
        if (useSpecial)
            charPool.append(SPECIAL);

        String pool = charPool.toString();
        if (excludeSimilar) {
            for (char c : SIMILAR.toCharArray()) {
                pool = pool.replace(String.valueOf(c), "");
            }
        }

        if (pool.isEmpty()) {
            throw new IllegalArgumentException("At least one character set must be selected");
        }

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(pool.charAt(random.nextInt(pool.length())));
        }

        // Shuffle to ensure randomness (though it's already generated randomly)
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray())
            chars.add(c);
        Collections.shuffle(chars);

        StringBuilder result = new StringBuilder();
        for (char c : chars)
            result.append(c);

        return result.toString();
    }

    public List<String> generatePasswords(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar, int quantity) {
        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            passwords.add(generatePassword(length, useUpper, useLower, useDigits, useSpecial, excludeSimilar));
        }
        return passwords;
    }

    public String calculateStrength(String password) {
        int score = 0;
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*[0-9].*"))
            score++;
        if (password.matches(".*[!@#$%^&*()\\-_+={}\\[\\]|;:,.<>?].*"))
            score++;

        if (score <= 2)
            return "Weak";
        if (score <= 4)
            return "Medium";
        if (score <= 5)
            return "Strong";
        return "Very Strong";
    }
}
