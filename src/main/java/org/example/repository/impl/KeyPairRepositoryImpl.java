package org.example.repository.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.repository.KeyPairRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyPairRepositoryImpl implements KeyPairRepository {

    private static final String STORAGE_DIR = "./data";
    private static final String KEY_FILE = "keypairs.json";
    private final Gson gson = new Gson();

    @Override
    public void saveKeyPairs(KeyPair senderKeyPair, KeyPair receiverKeyPair) {
        try {
            Path dirPath = Paths.get(STORAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            JsonObject json = new JsonObject();
            json.addProperty("senderPublicKey", Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded()));
            json.addProperty("senderPrivateKey", Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded()));
            json.addProperty("receiverPublicKey", Base64.getEncoder().encodeToString(receiverKeyPair.getPublic().getEncoded()));
            json.addProperty("receiverPrivateKey", Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded()));

            try (Writer writer = new FileWriter(getKeyFilePath())) {
                gson.toJson(json, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("保存密钥对失败: " + e.getMessage(), e);
        }
    }

    @Override
    public KeyPair[] loadKeyPairs() {
        if (!exists()) {
            return null;
        }

        try (Reader reader = new FileReader(getKeyFilePath())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] senderPubBytes = Base64.getDecoder().decode(json.get("senderPublicKey").getAsString());
            byte[] senderPrivBytes = Base64.getDecoder().decode(json.get("senderPrivateKey").getAsString());
            byte[] receiverPubBytes = Base64.getDecoder().decode(json.get("receiverPublicKey").getAsString());
            byte[] receiverPrivBytes = Base64.getDecoder().decode(json.get("receiverPrivateKey").getAsString());

            PublicKey senderPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(senderPubBytes));
            PrivateKey senderPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(senderPrivBytes));
            PublicKey receiverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(receiverPubBytes));
            PrivateKey receiverPrivateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(receiverPrivBytes));

            return new KeyPair[]{
                    new KeyPair(senderPublicKey, senderPrivateKey),
                    new KeyPair(receiverPublicKey, receiverPrivateKey)
            };
        } catch (Exception e) {
            throw new RuntimeException("加载密钥对失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(Paths.get(getKeyFilePath()));
    }

    private String getKeyFilePath() {
        return STORAGE_DIR + "/" + KEY_FILE;
    }
}