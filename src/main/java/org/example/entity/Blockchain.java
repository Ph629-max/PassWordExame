package org.example.entity;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> chain;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.chain.add(new Block(0, "0", null));
    }

    public Blockchain(List<Block> existingChain) {
        this.chain = existingChain;
    }

    public void addBlock(DigitalEnvelope data) {
        Block latestBlock = chain.get(chain.size() - 1);
        Block newBlock = new Block(chain.size(), latestBlock.hash, data);
        chain.add(newBlock);
    }

    public boolean verifyChain() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block prevBlock = chain.get(i - 1);

            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("【警告】区块 " + i + " 本身的数据已被篡改！");
                return false;
            }
            if (!currentBlock.prevHash.equals(prevBlock.hash)) {
                System.out.println("【警告】区块 " + i + " 与前驱区块的哈希链条断裂！");
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() { return chain; }
    public int getChainSize() { return chain.size(); }
    public Block getLatestBlock() { return chain.get(chain.size() - 1); }
}