package org.example.controller;

import org.example.entity.DigitalEnvelope;
import org.example.entity.Block;
import org.example.repository.KeyPairRepository;
import org.example.repository.impl.KeyPairRepositoryImpl;
import org.example.service.BlockchainService;
import org.example.service.CryptoService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.List;

public class MainController {
    private CryptoService cryptoService;
    private BlockchainService blockchainService;
    private KeyPair aliceKeyPair;
    private KeyPair bobKeyPair;
    private KeyPairRepository keyPairRepository;

    public MainController() {
        this.cryptoService = new CryptoService();
        this.blockchainService = new BlockchainService();
        this.keyPairRepository = new KeyPairRepositoryImpl();
        initializeKeys();
    }

    private void initializeKeys() {
        try {
            if (keyPairRepository.exists()) {
                KeyPair[] keyPairs = keyPairRepository.loadKeyPairs();
                aliceKeyPair = keyPairs[0];
                bobKeyPair = keyPairs[1];
            } else {
                aliceKeyPair = cryptoService.generateKeyPair();
                bobKeyPair = cryptoService.generateKeyPair();
                keyPairRepository.saveKeyPairs(aliceKeyPair, bobKeyPair);
            }
        } catch (Exception e) {
            throw new RuntimeException("密钥初始化失败", e);
        }
    }

    public String encryptAndStore(String plainText) throws Exception {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new IllegalArgumentException("明文数据不能为空！");
        }
        
        DigitalEnvelope envelope = cryptoService.encryptAndSign(plainText, aliceKeyPair, bobKeyPair);
        blockchainService.addBlock(envelope);
        blockchainService.saveToStorage();
        Block latestBlock = blockchainService.getLatestBlock();
        
        return String.format("存证成功！\n\n区块信息:\n• 区块索引: %d\n• 区块哈希: %s\n• 前驱哈希: %s\n\n加密摘要:\n• 原始明文长度: %d 字符\n• 使用算法: AES-256-CBC + RSA-2048",
                latestBlock.index, 
                truncateHash(latestBlock.hash), 
                truncateHash(latestBlock.prevHash),
                plainText.length());
    }

    public String decryptFromBlockchain(int blockIndex) throws Exception {
        if (blockIndex < 0) {
            throw new IllegalArgumentException("区块索引不能为负数！");
        }
        
        Block block = blockchainService.getBlockByIndex(blockIndex);
        if (block == null) {
            return "错误：指定区块不存在！";    
        }
        
        if (block.data == null) {
            return "错误：该区块没有数据！";
        }
        
        DigitalEnvelope envelope = block.data;
        String decryptedData = cryptoService.decryptAndVerify(envelope, aliceKeyPair, bobKeyPair);
        
        // 计算明文的SHA-256哈希值
        String plaintextHash = cryptoService.calculateSHA256(decryptedData);
        
        return String.format("解密成功！\n\n" +
                "【原始明文】\n%s\n\n" +
                "【哈希校验】\n" +
                "• 明文SHA-256哈希: %s\n\n" +
                "【区块信息】\n" +
                "• 区块索引: %d\n" +
                "• 区块哈希: %s\n" +
                "• 前驱哈希: %s\n" +
                "• 时间戳: %s",
                decryptedData,
                truncateHash(plaintextHash),
                blockIndex,
                truncateHash(block.hash),
                truncateHash(block.prevHash),
                block.timestamp);
    }

    public String verifyBlockchain() {
        boolean isValid = blockchainService.verifyChain();
        if (isValid) {
            return "区块链完整性校验通过！\n\n链状态:\n• 区块总数: " + blockchainService.getChainSize() + "\n• 所有区块哈希链条完整\n• 数据未被篡改";
        } else {
            return "区块链完整性校验失败！\n\n检测到以下问题:\n• 区块数据被篡改\n• 哈希链条断裂\n• 账本已被污染";
        }
    }

    public String verifySignature(int blockIndex) throws Exception {
        if (blockIndex < 0) {
            throw new IllegalArgumentException("区块索引不能为负数！");
        }
        
        Block block = blockchainService.getBlockByIndex(blockIndex);
        if (block == null) {
            return "错误：指定区块不存在！";
        }
        
        if (block.data == null) {
            return "错误：该区块没有数据！";
        }
        
        boolean isValid = cryptoService.verifySignature(block.data, aliceKeyPair);
        if (isValid) {
            return "数字签名验证通过！\n\n验证结果:\n• 数据完整性: 确认无误\n• 发送方身份: 已认证\n• 签名算法: SHA256withRSA";
        } else {
            return "数字签名验证失败！\n\n警告:\n• 数据可能被篡改\n• 发送方身份可疑\n• 签名与数据不匹配";    
        }
    }

    public String getBlockchainInfo() {
        List<Block> chain = blockchainService.getChain();
        StringBuilder info = new StringBuilder();
        info.append("区块链状态\n");
        info.append("===============\n");
        info.append(String.format("区块总数: %d\n", chain.size()));
        info.append(String.format("创世区块: #0\n\n", chain.size()));
        
        for (Block block : chain) {
            info.append(String.format("=== 区块 #%d ===\n", block.index));
            info.append(String.format("哈希: %s\n", truncateHash(block.hash)));
            info.append(String.format("前驱哈希: %s\n", truncateHash(block.prevHash)));
            info.append(String.format("时间戳: %s\n", formatTimestamp(block.timestamp)));
            info.append(String.format("数据: %s\n\n", block.data != null ? "存在数字信封" : "无数据"));
        }
        
        return info.toString();
    }

    public String tamperTest(int blockIndex, String fakeHash) {
        if (blockIndex < 0) {
            return "错误：区块索引不能为负数！";
        }
        
        if (blockIndex == 0) {
            return "错误：不能篡改创世区块！";
        }
        
        if (fakeHash == null || fakeHash.trim().isEmpty()) {
            return "错误：伪造哈希不能为空！";
        }
        
        boolean success = blockchainService.tamperBlock(blockIndex, fakeHash);
        blockchainService.saveToStorage();          
        if (success) {
            return String.format("模拟攻击成功！\n\n已篡改区块 #%d 的哈希值\n• 原始哈希已被替换为: %s\n\n建议: 运行区块链完整性校验验证防篡改机制", 
                    blockIndex, truncateHash(fakeHash));
        }
        return "错误：篡改失败，区块不存在";
    }

    public String getKeyInfo() {
        return String.format("密钥信息\n============\n\n发送方 (Alice) 公钥:\n%s\n\n接收方 (Bob) 公钥:\n%s",
                truncateKey(cryptoService.getEncodedKey(aliceKeyPair, true)),
                truncateKey(cryptoService.getEncodedKey(bobKeyPair, true)));
    }

    public int getBlockCount() {
        return blockchainService.getChainSize();
    }

    public String encryptImageFile(String inputPath, String outputPath) {
        try {
            if (!Files.exists(Paths.get(inputPath))) {
                return "错误：输入文件不存在！";    
            }
            
            cryptoService.encryptFile(inputPath, outputPath, bobKeyPair);
            return String.format("图片加密成功！\n\n文件信息:\n• 输入文件: %s\n• 输出文件: %s\n• 使用算法: AES-256-CBC + RSA-2048", 
                    inputPath, outputPath);
        } catch (Exception e) {
            return "加密失败: " + e.getMessage();
        }
    }

    public String decryptImageFile(String inputPath, String outputPath) {
        try {
            if (!Files.exists(Paths.get(inputPath))) {
                return "错误：输入文件不存在！";
            }
            
            cryptoService.decryptFile(inputPath, outputPath, bobKeyPair);
            
            byte[] decryptedBytes = Files.readAllBytes(Paths.get(outputPath));
            String formatHint = detectImageFormat(decryptedBytes);
            
            if (formatHint != null) {
                return String.format("图片解密成功！\n\n文件信息:\n• 输入文件: %s\n• 输出文件: %s\n• 检测格式: %s\n• 解密验证: 已通过完整性校验", 
                        inputPath, outputPath, formatHint);
            } else {
                return String.format("图片解密成功！\n\n文件信息:\n• 输入文件: %s\n• 输出文件: %s\n• 提示: 请确保输出文件使用正确的图片扩展名（如 .png, .jpg）", 
                        inputPath, outputPath);
            }
        } catch (javax.crypto.BadPaddingException | javax.crypto.IllegalBlockSizeException e) {
            return "解密失败: 密钥不匹配或文件已损坏！";
        } catch (Exception e) {
            return "解密失败: " + e.getMessage();
        }
    }

    private String detectImageFormat(byte[] data) {
        if (data == null || data.length < 4) return null;
        
        if (data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "PNG 图片";
        }
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return "JPEG/JPG 图片";
        }
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8') {
            return "GIF 图片";
        }
        if (data[0] == 'B' && data[1] == 'M') {
            return "BMP 图片";
        }
        if (data.length >= 12 && 
            data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F' &&
            data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "WebP 图片";
        }
        return null;
    }

    private String truncateHash(String hash) {
        if (hash == null) return "null";
        if (hash.length() <= 16) return hash;
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

    private String truncateKey(String key) {
        if (key == null) return "null";
        if (key.length() <= 64) return key;
        return key.substring(0, 32) + "\n...\n" + key.substring(key.length() - 32);
    }

    private String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}