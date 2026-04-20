package com.tuition.desktopapp.service;

public interface EncryptionService {

    String encrypt(String plainText);

    String decrypt(String cipherText);
}
