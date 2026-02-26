package com.revpasswordmanager.service;

public interface IPasswordGeneratorService {
    String generatePassword(int length, boolean useUpper, boolean useLower, boolean useDigits,
            boolean useSpecial, boolean excludeSimilar);

    String calculateStrength(String password);
}

