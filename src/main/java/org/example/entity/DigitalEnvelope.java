package org.example.entity;

public class DigitalEnvelope {
    private String encryptedData;     // AES加密后的核心明文数据
    private String encryptedAESKey;   // 接收方公钥加密后的AES会话密钥
    private String signature;         // 发送方数字签名

    public DigitalEnvelope() {}

    public DigitalEnvelope(String encryptedData, String encryptedAESKey, String signature) {
        this.encryptedData = encryptedData;
        this.encryptedAESKey = encryptedAESKey;
        this.signature = signature;
    }

    public String getEncryptedData() { return encryptedData; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
    public String getEncryptedAESKey() { return encryptedAESKey; }
    public void setEncryptedAESKey(String encryptedAESKey) { this.encryptedAESKey = encryptedAESKey; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}