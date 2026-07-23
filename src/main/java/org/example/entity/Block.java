package org.example.entity;

import com.google.gson.Gson;
import org.example.util.CryptoUtils;

public class Block {
    public int index;
    public long timestamp;
    public String prevHash;
    public String hash;
    public DigitalEnvelope data;

    public Block() {}

    public Block(int index, String prevHash, DigitalEnvelope data) {
        this.index = index;
        this.timestamp = System.currentTimeMillis();
        this.prevHash = prevHash;
        this.data = data;
        this.hash = calculateHash();
    }

    public String calculateHash() {
        Gson gson = new Gson();
        String dataString = gson.toJson(this.data);
        String rawInput = index + timestamp + prevHash + dataString;
        return CryptoUtils.sha256(rawInput);
    }
}