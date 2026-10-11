# 基于混合密码体制的微型存证系统（PassWordExame）

> 密码学课程设计 · Java Swing 桌面应用

## 📖 项目简介

本项目是一个**基于混合密码体制的微型区块链存证系统**。它把「数字信封 + 数字签名」的混合密码体制与「区块链哈希链」的不可篡改特性结合起来，实现数据的**加密存证、签名验签与链上完整性校验**，并配有可视化 Swing 图形界面。

## ✨ 功能特性

| 模块 | 能力 |
|------|------|
| 密钥管理 | RSA 密钥对生成、保存与加载（本地 JSON 持久化） |
| 混合加密 | AES 加密明文 + RSA 加密 AES 密钥与 IV（数字信封） |
| 完整性 | SHA/RSA 数字签名与验签，防篡改 |
| 区块链 | 区块生成、哈希链校验、创世块、链状态查询 |
| 持久化 | 区块链与密钥对落盘到 `data/` 目录 |
| 界面 | Swing GUI，支持加密、验签、查看链、校验链 |

## 🛠 技术栈

- **语言**：Java 8
- **构建**：Maven
- **密码学**：JCE（AES / RSA）+ BouncyCastle `bcprov-jdk18on:1.78`
- **序列化**：Gson `2.8.9`
- **测试**：JUnit `4.13.2`
- **界面**：Java Swing

## 📂 目录结构

```
PassWordExame/
├── data/                               # 运行时数据（区块链 / 密钥对）
│   ├── blockchain.json
│   └── keypairs.json
├── pom.xml
└── src/
    ├── main/java/org/example/
    │   ├── Main.java                    # 程序入口
    │   ├── controller/MainController.java
    │   ├── entity/                      # Block / Blockchain / DigitalEnvelope
    │   ├── repository/                  # 存储接口 + 实现
    │   ├── service/                     # BlockchainService / CryptoService
    │   ├── util/CryptoUtils.java        # 底层密码学原语
    │   └── view/MainGUI.java            # Swing 图形界面
    └── test/java/org/example/service/   # BlockchainServiceTest / CryptoServiceTest
```

## 🚀 快速开始

```bash
# 编译打包
mvn clean package

# 直接运行主类
mvn exec:java -Dexec.mainClass="org.example.Main"
# 或在 IDE 中运行 org.example.Main
```

## 🔐 设计要点

1. **数字信封**：每次加密随机生成 AES 会话密钥与 IV，用接收方 RSA 公钥加密后随密文一起传输，兼顾效率与安全。
2. **签名验签**：发送方以私钥对密文签名，接收方用公钥验签，验签失败直接抛出 `SecurityException`。
3. **哈希链**：每个区块记录前驱哈希，`verifyChain()` 可逐块校验，任一块被改动都会导致整链校验失败。

## ⚠️ 说明

- 本项目为教学/课程设计用途；`data/` 下的密钥文件请勿用于生产环境。
- 若更换运行目录，请确保程序对 `data/` 具有读写权限。
