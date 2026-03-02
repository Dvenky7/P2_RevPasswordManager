package com.revpasswordmanager.util;

public class TestRegexFix {
    public static void main(String[] args) {
        String[] passwords = {
                "abcABC123!",
                "test[bracket]",
                "another]bracket",
                "both[]brackets",
                "simplepassword",
                "12345678",
                "A1b2C3d4!",
                "vEry$tr0ngP@ssw0rd[]"
        };

        for (String pwd : passwords) {
            try {
                String strength = PasswordStrengthUtil.calculateStrength(pwd);
                System.out.println("Password: " + pwd + " -> Strength: " + strength);
            } catch (Exception e) {
                System.err.println("FAILED for password: " + pwd);
                e.printStackTrace();
            }
        }
    }
}
