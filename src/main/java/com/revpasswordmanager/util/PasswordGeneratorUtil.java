package com.revpasswordmanager.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGeneratorUtil {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String SIMILAR = "il1Lo0O";

    public static String generate(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar) {
        StringBuilder charPool = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        String l = excludeSimilar ? removeSimilar(LOWER) : LOWER;
        String u = excludeSimilar ? removeSimilar(UPPER) : UPPER;
        String d = excludeSimilar ? removeSimilar(DIGITS) : DIGITS;
        String s = excludeSimilar ? removeSimilar(SPECIAL) : SPECIAL;

        if (useLower) {
            charPool.append(l);
            mandatoryChars.add(l.charAt(random.nextInt(l.length())));
        }
        if (useUpper) {
            charPool.append(u);
            mandatoryChars.add(u.charAt(random.nextInt(u.length())));
        }
        if (useDigits) {
            charPool.append(d);
            mandatoryChars.add(d.charAt(random.nextInt(d.length())));
        }
        if (useSpecial) {
            charPool.append(s);
            mandatoryChars.add(s.charAt(random.nextInt(s.length())));
        }

        if (charPool.length() == 0) {
            charPool.append(l); // Fallback to lowercase
        }

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length - mandatoryChars.size(); i++) {
            password.append(charPool.charAt(random.nextInt(charPool.length())));
        }

        for (char c : mandatoryChars) {
            password.append(c);
        }

        List<Character> charList = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            charList.add(c);
        }
        Collections.shuffle(charList);

        StringBuilder result = new StringBuilder();
        for (char c : charList) {
            result.append(c);
        }

        return result.toString();
    }

    private static String removeSimilar(String pool) {
        StringBuilder sb = new StringBuilder();
        for (char c : pool.toCharArray()) {
            if (SIMILAR.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
