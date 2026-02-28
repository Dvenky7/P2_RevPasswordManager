package com.revpasswordmanager.service;

public interface IPasswordGeneratorService {
    String generatePassword(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar);

    java.util.List<String> generatePasswords(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar, int quantity);

    String calculateStrength(String password);
}
