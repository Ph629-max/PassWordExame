package org.example.service;

import org.example.entity.Block;
import org.example.entity.DigitalEnvelope;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BlockchainServiceTest {

    @Test
    public void testAddBlock() {
        BlockchainService service = new BlockchainService();
        
        service.saveToStorage();
        
        int initialSize = service.getChainSize();
        service.addBlock(new DigitalEnvelope("test", "key", "signature"));
        
        assertEquals(initialSize + 1, service.getChainSize());
    }

    @Test
    public void testVerifyChain() {
        BlockchainService service = new BlockchainService();
        
        assertTrue("新链应该验证通过", service.verifyChain());
        
        service.addBlock(new DigitalEnvelope("test", "key", "signature"));
        
        service.saveToStorage();
        
        assertTrue("添加区块后链应该验证通过", service.verifyChain());
    }

    @Test
    public void testTamperBlock() {
        BlockchainService service = new BlockchainService();
        service.addBlock(new DigitalEnvelope("test", "key", "signature"));
        
        assertTrue("初始链应该验证通过", service.verifyChain());
        
        service.tamperBlock(1, "fake_hash_1234567890");
        service.saveToStorage();

        assertFalse("篡改后链应该验证失败", service.verifyChain());
    }

    @Test
    public void testGetBlockByIndex() {
        BlockchainService service = new BlockchainService();
        
        Block genesis = service.getBlockByIndex(0);
        assertNotNull("创世区块应该存在", genesis);
        assertEquals(0, genesis.index);
        
        service.addBlock(new DigitalEnvelope("test", "key", "signature"));
        service.saveToStorage();

        Block block1 = service.getBlockByIndex(1);
        assertNotNull("区块1应该存在", block1);
        assertEquals(1, block1.index);
        
        Block invalid = service.getBlockByIndex(100);
        assertNull("无效索引应该返回null", invalid);
    }

    @Test
    public void testGenesisBlock() {
        BlockchainService service = new BlockchainService();
        
        List<Block> chain = service.getChain();
        assertFalse("链不应该为空", chain.isEmpty());
        
        Block genesis = chain.get(0);
        assertEquals(0, genesis.index);
        assertEquals("0", genesis.prevHash);
    }
}