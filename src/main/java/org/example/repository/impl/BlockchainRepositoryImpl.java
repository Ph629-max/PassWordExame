package org.example.repository.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.entity.Block;
import org.example.repository.BlockchainRepository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BlockchainRepositoryImpl implements BlockchainRepository {

    private static final String STORAGE_DIR = "./data";
    private static final String CHAIN_FILE = "blockchain.json";
    private final Gson gson = new Gson();

    public BlockchainRepositoryImpl() {
        try {
            Path dirPath = Paths.get(STORAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveChain(List<Block> chain) {
        try (Writer writer = new FileWriter(getChainFilePath())) {
            gson.toJson(chain, writer);
        } catch (IOException e) {
            throw new RuntimeException("保存区块链失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Block> loadChain() {
        if (!exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(getChainFilePath())) {
            Type listType = new TypeToken<List<Block>>() {}.getType();
            List<Block> chain = gson.fromJson(reader, listType);
            
            if (chain == null || chain.isEmpty()) {
                return new ArrayList<>();
            }
            
            return chain;
        } catch (IOException e) {
            throw new RuntimeException("加载区块链失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(Paths.get(getChainFilePath()));
    }

    @Override
    public void deleteChain() {
        try {
            Files.deleteIfExists(Paths.get(getChainFilePath()));
        } catch (IOException e) {
            throw new RuntimeException("删除区块链失败: " + e.getMessage(), e);
        }
    }

    private String getChainFilePath() {
        return STORAGE_DIR + "/" + CHAIN_FILE;
    }
}