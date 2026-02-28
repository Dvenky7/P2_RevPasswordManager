package com.revpasswordmanager.util;

public class PasswordStrengthUtil {

    public static String calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Too Short";
        }

        int score = 0;
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[0-9].*"))
            score++;
        if (password.matches(".*[!@#$%^&*()\\-_=+[\\]{}|;:,.<>?].*"))
            score++;

        if (score < 3)
            return "Weak";
        if (score < 5)
            return "Medium";
        return "Strong";
    }

    public static boolean isWeak(String password) {
        return "Weak".equalsIgnoreCase(calculateStrength(password));
    }
}
