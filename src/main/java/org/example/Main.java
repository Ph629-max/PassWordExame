package org.example;

import org.example.view.MainGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   [SYSTEM WATERMARK] 姓名: 陈作源 | 学号: 2025031004");
        System.out.println("   [SYSTEM STATUS] 基于混合密码体制的微型存证系统启动...");
        System.out.println("==========================================================");

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}