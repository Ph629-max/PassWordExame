package org.example.service;

import org.example.entity.Block;
import org.example.entity.Blockchain;
import org.example.entity.DigitalEnvelope;
import org.example.repository.BlockchainRepository;
import org.example.repository.impl.BlockchainRepositoryImpl;

import java.util.List;

public class BlockchainService {
    private Blockchain blockchain;
    private BlockchainRepository repository;

    public BlockchainService() {
        this.repository = new BlockchainRepositoryImpl();
        loadFromStorage();
    }

    private void loadFromStorage() {
        if (repository.exists()) {
            List<Block> savedChain = repository.loadChain();
            if (!savedChain.isEmpty()) {
                blockchain = new Blockchain(savedChain);
                return;
            }
        }
        blockchain = new Blockchain();
    }

    public void saveToStorage() {
        repository.saveChain(blockchain.getChain());
    }

    public void addBlock(DigitalEnvelope data) {
        blockchain.addBlock(data);
    }

    public boolean verifyChain() {
        return blockchain.verifyChain();
    }

    public List<Block> getChain() {
        return blockchain.getChain();
    }

    public int getChainSize() {
        return blockchain.getChainSize();
    }

    public Block getLatestBlock() {
        return blockchain.getLatestBlock();
    }

    public Block getBlockByIndex(int index) {
        List<Block> chain = blockchain.getChain();
        if (index >= 0 && index < chain.size()) {
            return chain.get(index);
        }
        return null;
    }

    public String getBlockInfo(Block block) {
        if (block == null) return "区块不存在";
        return String.format("索引: %d\n时间戳: %d\n哈希: %s\n前驱哈希: %s",
                block.index, block.timestamp, block.hash, block.prevHash);
    }

    public boolean tamperBlock(int index, String newHash) {
        Block block = getBlockByIndex(index);
        if (block != null) {
            block.hash = newHash;
            return true;
        }
        return false;
    }
}