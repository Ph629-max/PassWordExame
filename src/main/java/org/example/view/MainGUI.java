package org.example.view;

import org.example.controller.MainController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

public class MainGUI extends JFrame {
    private MainController controller;
    private JTextArea encryptLogArea;
    private JTextArea decryptLogArea;
    private JTextArea imageLogArea;
    private JTextArea auditLogArea;
    private JTextArea testLogArea;
    private JTextField inputField;
    private JTextField blockIndexField;
    private JTextField inputFilePath;
    private JTextField outputFilePath;
    private JLabel statusLabel;
    private JPanel watermarkPanel;
    private JTabbedPane tabbedPane;

    public MainGUI() {
        super("基于混合密码体制的微型存证系统");
        this.controller = new MainController();
        initGUI();
    }

    private void initGUI() {
        this.setSize(1000, 750);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout(10, 10));
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        initStatusBar();
        initWatermarkPanel();

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        
        this.tabbedPane.addTab("文本加密存证", createEncryptPanel());
        this.tabbedPane.addTab("文本解密验证", createDecryptPanel());
        this.tabbedPane.addTab("图片加解密", createImagePanel());
        this.tabbedPane.addTab("区块链审计", createAuditPanel());
        this.tabbedPane.addTab("破坏性测试", createTestPanel());

        this.add(statusLabel, BorderLayout.NORTH);
        this.add(this.tabbedPane, BorderLayout.CENTER);
        this.add(watermarkPanel, BorderLayout.SOUTH);
    }

    private void initStatusBar() {
        statusLabel = new JLabel("系统就绪 | 区块总数: 1 | 密钥已初始化");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0, 150, 0));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void initWatermarkPanel() {
        watermarkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        watermarkPanel.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel watermarkLabel = new JLabel("姓名: 陈作源 | 学号: 2025031004");
        watermarkLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        watermarkLabel.setForeground(new Color(100, 100, 100));
        
        watermarkPanel.add(watermarkLabel);
    }

    private void updateStatus(String message, boolean success) {
        statusLabel.setText((success ? "[OK]" : "[ERR]") + " " + message + " | 区块总数: " + controller.getBlockCount());
        statusLabel.setForeground(success ? new Color(0, 150, 0) : Color.RED);
    }

    private JPanel createEncryptPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new TitledBorder("输入待存证明文"));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        inputField.setToolTipText("请输入要加密存证的文本内容");
        
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JButton encryptBtn = new JButton("加密并上链存证");
        encryptBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        encryptBtn.setPreferredSize(new Dimension(180, 40));
        encryptBtn.addActionListener(e -> performEncrypt());

        JButton clearBtn = new JButton("清空输入");
        clearBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        clearBtn.setPreferredSize(new Dimension(120, 40));
        clearBtn.addActionListener(e -> {
            inputField.setText("");
            encryptLogArea.setText("");
            updateStatus("输入已清空", true);
        });

        btnPanel.add(encryptBtn);
        btnPanel.add(clearBtn);

        encryptLogArea = createLogArea();
        JScrollPane logScrollPane = new JScrollPane(encryptLogArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(900, 200));
        logScrollPane.setMinimumSize(new Dimension(400, 150));

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScrollPane);

        return panel;
    }

    private JPanel createDecryptPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        inputPanel.setBorder(new TitledBorder("解密参数"));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        inputPanel.add(new JLabel("区块索引:"));
        blockIndexField = new JTextField(6);
        blockIndexField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        blockIndexField.setToolTipText("请输入要解密的区块索引（从1开始）");
        inputPanel.add(blockIndexField);

        JButton quickBtn1 = new JButton("区块 #1");
        quickBtn1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        quickBtn1.addActionListener(e -> blockIndexField.setText("1"));
        inputPanel.add(quickBtn1);

        JButton quickBtn2 = new JButton("最新区块");
        quickBtn2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        quickBtn2.addActionListener(e -> blockIndexField.setText(String.valueOf(controller.getBlockCount() - 1)));
        inputPanel.add(quickBtn2);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JButton decryptBtn = new JButton("解密并验证");
        decryptBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        decryptBtn.setPreferredSize(new Dimension(150, 40));
        decryptBtn.addActionListener(e -> performDecrypt());

        JButton verifySigBtn = new JButton("仅验证签名");
        verifySigBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        verifySigBtn.setPreferredSize(new Dimension(150, 40));
        verifySigBtn.addActionListener(e -> performVerifySignature());

        btnPanel.add(decryptBtn);
        btnPanel.add(verifySigBtn);

        decryptLogArea = createLogArea();
        JScrollPane logScrollPane = new JScrollPane(decryptLogArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(900, 200));
        logScrollPane.setMinimumSize(new Dimension(400, 150));

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScrollPane);

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JPanel srcPanel = new JPanel(new BorderLayout(5, 5));
        srcPanel.setBorder(new TitledBorder("源文件路径"));
        inputFilePath = new JTextField();
        inputFilePath.setEditable(false);
        inputFilePath.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        JButton browseSrcBtn = new JButton("浏览...");
        browseSrcBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        browseSrcBtn.addActionListener(e -> browseFile(inputFilePath, false));
        srcPanel.add(inputFilePath, BorderLayout.CENTER);
        srcPanel.add(browseSrcBtn, BorderLayout.EAST);

        JPanel destPanel = new JPanel(new BorderLayout(5, 5));
        destPanel.setBorder(new TitledBorder("输出文件路径"));
        outputFilePath = new JTextField();
        outputFilePath.setEditable(false);
        outputFilePath.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        JButton browseDestBtn = new JButton("浏览...");
        browseDestBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        browseDestBtn.addActionListener(e -> browseFile(outputFilePath, true));
        destPanel.add(outputFilePath, BorderLayout.CENTER);
        destPanel.add(browseDestBtn, BorderLayout.EAST);

        inputPanel.add(srcPanel);
        inputPanel.add(destPanel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JButton encryptImgBtn = new JButton("图片加密");
        encryptImgBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        encryptImgBtn.setPreferredSize(new Dimension(150, 40));
        encryptImgBtn.addActionListener(e -> performImageEncrypt());

        JButton decryptImgBtn = new JButton("图片解密");
        decryptImgBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        decryptImgBtn.setPreferredSize(new Dimension(150, 40));
        decryptImgBtn.addActionListener(e -> performImageDecrypt());

        btnPanel.add(encryptImgBtn);
        btnPanel.add(decryptImgBtn);

        imageLogArea = createLogArea();
        JScrollPane logScrollPane = new JScrollPane(imageLogArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(900, 200));
        logScrollPane.setMinimumSize(new Dimension(400, 150));

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScrollPane);

        return panel;
    }

    private JPanel createAuditPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JButton verifyChainBtn = new JButton("验证区块链完整性");
        verifyChainBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        verifyChainBtn.setPreferredSize(new Dimension(200, 40));
        verifyChainBtn.addActionListener(e -> performVerifyChain());

        JButton showChainBtn = new JButton("查看区块链信息");
        showChainBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        showChainBtn.setPreferredSize(new Dimension(200, 40));
        showChainBtn.addActionListener(e -> showBlockchainInfo());

        JButton showKeysBtn = new JButton("查看密钥信息");
        showKeysBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        showKeysBtn.setPreferredSize(new Dimension(200, 40));
        showKeysBtn.addActionListener(e -> showKeyInfo());

        btnPanel.add(verifyChainBtn);
        btnPanel.add(showChainBtn);
        btnPanel.add(showKeysBtn);

        auditLogArea = createLogArea();
        JScrollPane logScrollPane = new JScrollPane(auditLogArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(900, 200));
        logScrollPane.setMinimumSize(new Dimension(400, 150));

        panel.add(btnPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScrollPane);

        return panel;
    }

    private JPanel createTestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        inputPanel.setBorder(new TitledBorder("破坏性测试参数"));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        inputPanel.add(new JLabel("篡改区块索引:"));
        JTextField tamperIndexField = new JTextField(6);
        tamperIndexField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        inputPanel.add(tamperIndexField);

        inputPanel.add(new JLabel("伪造哈希:"));
        JTextField fakeHashField = new JTextField(40);
        fakeHashField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        fakeHashField.setText("f123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        inputPanel.add(fakeHashField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JButton tamperBtn = new JButton("模拟篡改攻击");
        tamperBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        tamperBtn.setPreferredSize(new Dimension(200, 40));
        tamperBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "此操作将篡改区块链数据，用于测试防篡改机制。\n确定要继续吗？", 
                    "确认操作", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            
            try {
                int index = Integer.parseInt(tamperIndexField.getText());
                String fakeHash = fakeHashField.getText();
                String result = controller.tamperTest(index, fakeHash);
                logMessage("模拟攻击", result);
                
                if (result.contains("成功")) {
                    String verifyResult = controller.verifyBlockchain();
                    logMessage("自动验证", verifyResult);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的区块索引（数字）", "输入错误", JOptionPane.WARNING_MESSAGE);
                logMessage("错误", "请输入有效的区块索引");
            }
        });

        JButton autoTestBtn = new JButton("自动运行完整测试");
        autoTestBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        autoTestBtn.setPreferredSize(new Dimension(200, 40));
        autoTestBtn.addActionListener(e -> runAutoTest());

        btnPanel.add(tamperBtn);
        btnPanel.add(autoTestBtn);

        testLogArea = createLogArea();
        JScrollPane logScrollPane = new JScrollPane(testLogArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.setPreferredSize(new Dimension(900, 200));
        logScrollPane.setMinimumSize(new Dimension(400, 150));

        panel.add(inputPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logScrollPane);

        return panel;
    }

    private JTextArea createLogArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(new Color(25, 25, 25));
        area.setForeground(new Color(0, 255, 64));
        area.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)), 
                "操作日志", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Microsoft YaHei", Font.PLAIN, 12), new Color(100, 100, 100)));
        area.setRows(8);  // 设置行数限制，超过时自动滚动
        area.setColumns(80);  // 设置列数
        return area;
    }

    private JButton createButton(String text, int width, int height, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(width, height));
        button.addActionListener(listener);
        return button;
    }

    private void browseFile(JTextField targetField, boolean saveMode) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (saveMode) {
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                targetField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        } else {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                targetField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private void performEncrypt() {
        String plainText = inputField.getText().trim();
        
        if (plainText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入待存证的明文数据！", "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", "明文数据不能为空");
            return;
        }

        try {
            logMessage("正在处理", "正在生成密钥对并执行加密...");
            String result = controller.encryptAndStore(plainText);
            logMessage("存证结果", result);
            updateStatus("文本存证成功", true);
            JOptionPane.showMessageDialog(this, "加密存证成功！数据已写入区块链", "操作成功", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logMessage("错误", "加密存证失败: " + e.getMessage());
            updateStatus("存证失败", false);
                JOptionPane.showMessageDialog(this, "加密存证失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDecrypt() {
        String indexStr = blockIndexField.getText().trim();
        
        if (indexStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入区块索引！", "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", "区块索引不能为空");
            return;
        }

        try {
            int index = Integer.parseInt(indexStr);
            String result = controller.decryptFromBlockchain(index);
            logMessage("解密结果", result);
            
            if (result.contains("成功")) {
                updateStatus("解密成功", true);
                JOptionPane.showMessageDialog(this, "解密成功！", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                updateStatus("解密失败", false);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字索引！", "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", "无效的区块索引格式");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", e.getMessage());
        } catch (Exception e) {
            logMessage("错误", "解密失败: " + e.getMessage());
            updateStatus("解密失败", false);
            JOptionPane.showMessageDialog(this, "解密失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performVerifySignature() {
        String indexStr = blockIndexField.getText().trim();
        
        if (indexStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入区块索引！", "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", "区块索引不能为空");
            return;
        }

        try {
            int index = Integer.parseInt(indexStr);
            String result = controller.verifySignature(index);
            logMessage("签名验证", result);
            
            if (result.contains("通过")) {
                updateStatus("签名验证通过", true);
            } else {
                updateStatus("签名验证失败", false);
                JOptionPane.showMessageDialog(this, "签名验证失败！数据可能被篡改", "验证失败", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字索引！", "输入错误", JOptionPane.WARNING_MESSAGE);
            logMessage("错误", "无效的区块索引格式");
        } catch (Exception e) {
            logMessage("错误", "签名验证失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "签名验证失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performVerifyChain() {
        String result = controller.verifyBlockchain();
        logMessage("完整性验证", result);
        
        if (result.contains("通过")) {
            updateStatus("区块链完整", true);
            JOptionPane.showMessageDialog(this, "区块链完整性校验通过！", "验证成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            updateStatus("区块链已被篡改", false);
            JOptionPane.showMessageDialog(this, "区块链完整性校验失败！检测到数据篡改", "验证失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBlockchainInfo() {
        String info = controller.getBlockchainInfo();
        logMessage("区块链信息", info);
        updateStatus("已查看区块链信息", true);
    }

    private void showKeyInfo() {
        String info = controller.getKeyInfo();
        logMessage("密钥信息", info);
        updateStatus("已查看密钥信息", true);
    }

    private void performImageEncrypt() {
        String inputPath = inputFilePath.getText().trim();
        String outputPath = outputFilePath.getText().trim();
        
        if (inputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输入文件！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输出文件！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this, "输入文件不存在！", "文件错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            logMessage("正在处理", "正在加密图片文件...");
            String result = controller.encryptImageFile(inputPath, outputPath);
            logMessage("图片加密", result);
            
            if (result.contains("成功")) {
                updateStatus("图片加密成功", true);
                JOptionPane.showMessageDialog(this, "图片加密成功！", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                updateStatus("图片加密失败", false);
                JOptionPane.showMessageDialog(this, result, "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logMessage("错误", "图片加密失败: " + e.getMessage());
            updateStatus("图片加密失败", false);
            JOptionPane.showMessageDialog(this, "图片加密失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performImageDecrypt() {
        String inputPath = inputFilePath.getText().trim();
        String outputPath = outputFilePath.getText().trim();
        
        if (inputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输入文件！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择输出文件！", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this, "输入文件不存在！", "文件错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            logMessage("正在处理", "正在解密图片文件...");
            String result = controller.decryptImageFile(inputPath, outputPath);
            logMessage("图片解密", result);
            
            if (result.contains("成功")) {
                updateStatus("图片解密成功", true);
                JOptionPane.showMessageDialog(this, "图片解密成功！", "操作成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                updateStatus("图片解密失败", false);
                JOptionPane.showMessageDialog(this, result, "操作失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logMessage("错误", "图片解密失败: " + e.getMessage());
            updateStatus("图片解密失败", false);
            JOptionPane.showMessageDialog(this, "图片解密失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runAutoTest() {
        logMessage("自动测试", "=== 开始自动破坏性测试 ===\n");
        
        try {
            logMessage("步骤1", "验证初始区块链完整性...");
            String verify1 = controller.verifyBlockchain();
            logMessage("结果", verify1);
            
            logMessage("步骤2", "模拟篡改区块 #1...");
            String tamper = controller.tamperTest(1, "attacker_fake_hash_12345");
            logMessage("结果", tamper);
            
            logMessage("步骤3", "验证篡改后的区块链完整性...");
            String verify2 = controller.verifyBlockchain();
            logMessage("结果", verify2);
            
            logMessage("测试完成", "=== 自动测试完成 ===\n" + 
                    (verify2.contains("失败") ? "防篡改机制正常工作！" : "防篡改机制存在漏洞！"));
            
            if (verify2.contains("失败")) {
                updateStatus("防篡改测试通过", true);
                JOptionPane.showMessageDialog(this, "自动测试完成！防篡改机制正常工作！", "测试成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                updateStatus("防篡改测试失败", false);
                JOptionPane.showMessageDialog(this, "防篡改机制存在漏洞！", "测试失败", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logMessage("错误", "自动测试失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "自动测试失败: " + e.getMessage(), "操作失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logMessage(String title, String message) {
        JTextArea targetLogArea = getCurrentLogArea();
        if (targetLogArea != null) {
            targetLogArea.append(String.format("[%s]\n%s\n\n", title, message));
            targetLogArea.setCaretPosition(targetLogArea.getDocument().getLength());
        }
    }

    private JTextArea getCurrentLogArea() {
        if (this.tabbedPane == null) {
            return encryptLogArea;
        }
        int selectedIndex = this.tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0: return encryptLogArea;
            case 1: return decryptLogArea;
            case 2: return imageLogArea;
            case 3: return auditLogArea;
            case 4: return testLogArea;
            default: return encryptLogArea;
        }
    }

    public static void main(String[] args) {
        Font globalFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, globalFont);
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}