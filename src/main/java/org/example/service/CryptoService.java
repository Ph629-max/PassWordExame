package org.example.service;

import org.example.entity.DigitalEnvelope;
import org.example.util.CryptoUtils;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.Base64;

public class CryptoService {

    public KeyPair generateKeyPair() throws Exception {
        return CryptoUtils.generateRSAKeyPair();
    }

    public DigitalEnvelope encryptAndSign(String plainText, KeyPair senderKeyPair, KeyPair receiverKeyPair) throws Exception {
        SecretKey aesKey = CryptoUtils.generateAESKey();
        byte[] iv = CryptoUtils.generateIV();

        String cipherText = CryptoUtils.aesEncrypt(plainText, aesKey, iv);
        
        byte[] rawKeyBytes = aesKey.getEncoded();
        byte[] keyAndIvBytes = new byte[rawKeyBytes.length + iv.length];
        System.arraycopy(rawKeyBytes, 0, keyAndIvBytes, 0, rawKeyBytes.length);
        System.arraycopy(iv, 0, keyAndIvBytes, rawKeyBytes.length, iv.length);

        String encryptedAESKey = CryptoUtils.rsaEncrypt(keyAndIvBytes, receiverKeyPair.getPublic());
        String signature = CryptoUtils.sign(cipherText, senderKeyPair.getPrivate());

        return new DigitalEnvelope(cipherText, encryptedAESKey, signature);
    }

    public String decryptAndVerify(DigitalEnvelope envelope, KeyPair senderKeyPair, KeyPair receiverKeyPair) throws Exception {
        boolean isSignatureValid = CryptoUtils.verify(
                envelope.getEncryptedData(),
                envelope.getSignature(),
                senderKeyPair.getPublic()
        );

        if (!isSignatureValid) {
            throw new SecurityException("数字签名验证失败！数据可能被篡改或身份非法");
        }

        byte[] decryptedKeyAndIv = CryptoUtils.rsaDecrypt(envelope.getEncryptedAESKey(), receiverKeyPair.getPrivate());
        byte[] decryptedKeyBytes = new byte[decryptedKeyAndIv.length - 16];
        byte[] decryptedIv = new byte[16];
        System.arraycopy(decryptedKeyAndIv, 0, decryptedKeyBytes, 0, decryptedKeyBytes.length);
        System.arraycopy(decryptedKeyAndIv, decryptedKeyBytes.length, decryptedIv, 0, 16);

        SecretKey restoredSecretKey = new javax.crypto.spec.SecretKeySpec(decryptedKeyBytes, "AES");
        return CryptoUtils.aesDecrypt(envelope.getEncryptedData(), restoredSecretKey, decryptedIv);
    }

    public boolean verifySignature(DigitalEnvelope envelope, KeyPair senderKeyPair) throws Exception {
        return CryptoUtils.verify(envelope.getEncryptedData(), envelope.getSignature(), senderKeyPair.getPublic());
    }

    public String getEncodedKey(KeyPair keyPair, boolean isPublic) {
        byte[] keyBytes = isPublic ? keyPair.getPublic().getEncoded() : keyPair.getPrivate().getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    // 图片加密：返回加密后的字节数组
    public byte[] encryptImage(byte[] imageBytes, KeyPair receiverKeyPair) throws Exception {
        SecretKey aesKey = CryptoUtils.generateAESKey();
        byte[] iv = CryptoUtils.generateIV();

        String cipherText = CryptoUtils.aesEncrypt(Base64.getEncoder().encodeToString(imageBytes), aesKey, iv);
        byte[] cipherBytes = cipherText.getBytes("UTF-8");

        byte[] rawKeyBytes = aesKey.getEncoded();
        byte[] keyAndIvBytes = new byte[rawKeyBytes.length + iv.length];
        System.arraycopy(rawKeyBytes, 0, keyAndIvBytes, 0, rawKeyBytes.length);
        System.arraycopy(iv, 0, keyAndIvBytes, rawKeyBytes.length, iv.length);

        String encryptedKeyBase64 = CryptoUtils.rsaEncrypt(keyAndIvBytes, receiverKeyPair.getPublic());
        byte[] encryptedKey = encryptedKeyBase64.getBytes("UTF-8");
        
        ByteBuffer buffer = ByteBuffer.allocate(4 + encryptedKey.length + cipherBytes.length);
        buffer.putInt(encryptedKey.length);
        buffer.put(encryptedKey);
        buffer.put(cipherBytes);

        return buffer.array();
    }

    // 图片解密
    public byte[] decryptImage(byte[] encryptedData, KeyPair receiverKeyPair) throws Exception {
        if (encryptedData == null || encryptedData.length < 5) {
            throw new IllegalArgumentException("加密数据无效，长度不足");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        int keyLength = buffer.getInt();
        
        if (keyLength <= 0 || keyLength > 1024) {
            throw new IllegalArgumentException("无效的密钥长度: " + keyLength);
        }
        
        byte[] encryptedKey = new byte[keyLength];
        buffer.get(encryptedKey);
        
        if (buffer.remaining() <= 0) {
            throw new IllegalArgumentException("加密数据不完整，缺少密文部分");
        }
        
        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);

        String encryptedKeyBase64 = new String(encryptedKey, "UTF-8");
        byte[] decryptedKeyAndIv = CryptoUtils.rsaDecrypt(encryptedKeyBase64, receiverKeyPair.getPrivate());
        
        if (decryptedKeyAndIv.length < 16) {
            throw new IllegalArgumentException("解密后的密钥数据不完整");
        }
        
        byte[] decryptedKeyBytes = new byte[decryptedKeyAndIv.length - 16];
        byte[] decryptedIv = new byte[16];
        System.arraycopy(decryptedKeyAndIv, 0, decryptedKeyBytes, 0, decryptedKeyBytes.length);
        System.arraycopy(decryptedKeyAndIv, decryptedKeyBytes.length, decryptedIv, 0, 16);

        SecretKey restoredSecretKey = new javax.crypto.spec.SecretKeySpec(decryptedKeyBytes, "AES");
        String cipherText = new String(cipherBytes, "UTF-8");
        String decodedText = CryptoUtils.aesDecrypt(cipherText, restoredSecretKey, decryptedIv);

        byte[] result = Base64.getDecoder().decode(decodedText);
        
        if (result.length == 0) {
            throw new IllegalArgumentException("解密后数据为空");
        }
        
        return result;
    }

    // 文件加密
    public void encryptFile(String inputPath, String outputPath, KeyPair receiverKeyPair) throws Exception {
        byte[] fileBytes = Files.readAllBytes(Paths.get(inputPath));
        byte[] encryptedBytes = encryptImage(fileBytes, receiverKeyPair);
        Files.write(Paths.get(outputPath), encryptedBytes);
    }

    // 文件解密
    public void decryptFile(String inputPath, String outputPath, KeyPair receiverKeyPair) throws Exception {
        byte[] encryptedBytes = Files.readAllBytes(Paths.get(inputPath));
        byte[] decryptedBytes = decryptImage(encryptedBytes, receiverKeyPair);
        Files.write(Paths.get(outputPath), decryptedBytes);
    }

    // 计算字符串的 SHA-256 哈希值
    public String calculateSHA256(String input) throws Exception {
        return CryptoUtils.calculateSHA256(input);
    }
}