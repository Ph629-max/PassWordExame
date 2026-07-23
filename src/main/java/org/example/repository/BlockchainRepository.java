package org.example.repository;

import org.example.entity.Block;

import java.util.List;

public interface BlockchainRepository {
    void saveChain(List<Block> chain);
    List<Block> loadChain();
    boolean exists();
    void deleteChain();
}