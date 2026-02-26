package com.revpasswordmanager.service;

public interface IEncryptionService {
    String encrypt(String data) throws Exception;

    String decrypt(String encryptedData) throws Exception;
}

