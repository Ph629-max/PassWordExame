package org.example.service;

import org.example.entity.DigitalEnvelope;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class CryptoServiceTest {

    @Test
    public void testGenerateKeyPair() throws Exception {
        CryptoService service = new CryptoService();
        KeyPair keyPair = service.generateKeyPair();
        
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        CryptoService service = new CryptoService();
        KeyPair senderKeyPair = service.generateKeyPair();
        KeyPair receiverKeyPair = service.generateKeyPair();
        
        String plainText = "测试加密解密功能";
        
        DigitalEnvelope envelope = service.encryptAndSign(plainText, senderKeyPair, receiverKeyPair);
        
        assertNotNull(envelope);
        assertNotNull(envelope.getEncryptedData());
        assertNotNull(envelope.getEncryptedAESKey());
        assertNotNull(envelope.getSignature());
        
        String decryptedText = service.decryptAndVerify(envelope, senderKeyPair, receiverKeyPair);
        
        assertEquals(plainText, decryptedText);
    }

    @Test
    public void testSignatureVerification() throws Exception {
        CryptoService service = new CryptoService();
        KeyPair senderKeyPair = service.generateKeyPair();
        KeyPair receiverKeyPair = service.generateKeyPair();
        
        String plainText = "测试签名验证";
        
        DigitalEnvelope envelope = service.encryptAndSign(plainText, senderKeyPair, receiverKeyPair);
        
        boolean isValid = service.verifySignature(envelope, senderKeyPair);
        assertTrue("签名验证应该通过", isValid);
        
        DigitalEnvelope tamperedEnvelope = new DigitalEnvelope(
                envelope.getEncryptedData() + "tampered",
                envelope.getEncryptedAESKey(),
                envelope.getSignature()
        );
        
        boolean isTamperedValid = service.verifySignature(tamperedEnvelope, senderKeyPair);
        assertFalse("篡改后签名验证应该失败", isTamperedValid);
    }

    @Test(expected = SecurityException.class)
    public void testDecryptWithTamperedSignature() throws Exception {
        CryptoService service = new CryptoService();
        KeyPair senderKeyPair = service.generateKeyPair();
        KeyPair receiverKeyPair = service.generateKeyPair();
        
        String plainText = "测试篡改签名";
        
        DigitalEnvelope envelope = service.encryptAndSign(plainText, senderKeyPair, receiverKeyPair);
        
        DigitalEnvelope tamperedEnvelope = new DigitalEnvelope(
                envelope.getEncryptedData() + "tampered",
                envelope.getEncryptedAESKey(),
                envelope.getSignature()
        );
        
        service.decryptAndVerify(tamperedEnvelope, senderKeyPair, receiverKeyPair);
    }
}