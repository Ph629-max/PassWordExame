package org.example.repository;

import java.security.KeyPair;

public interface KeyPairRepository {
    void saveKeyPairs(KeyPair senderKeyPair, KeyPair receiverKeyPair);
    KeyPair[] loadKeyPairs();
    boolean exists();
}