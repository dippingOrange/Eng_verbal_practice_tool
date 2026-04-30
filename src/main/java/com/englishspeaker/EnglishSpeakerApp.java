package com.englishspeaker;

import com.englishspeaker.gui.*;
import com.englishspeaker.service.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class EnglishSpeakerApp extends JFrame {
    private static final String SETUP = "SETUP";
    private static final String HOME = "HOME";
    private static final String READING = "READING";
    private static final String CONVERSATION = "CONVERSATION";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private AiService aiService;
    private AlibabaAsrService alibabaAsrService;
    private ReadingService readingService;
    private ConversationService conversationService;
    private HomePanel homePanel;
    private ConversationPanel conversationPanel;

    public EnglishSpeakerApp() throws Exception {
        setTitle("English Speaking Practice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        if (isConfigComplete()) {
            initializeServices();
            createAndAddPanels();
        } else {
            SetupPanel setupPanel = new SetupPanel(this::completeSetup);
            mainPanel.add(setupPanel, SETUP);
        }

        add(mainPanel);

        if (isConfigComplete()) {
            cardLayout.show(mainPanel, HOME);
        } else {
            cardLayout.show(mainPanel, SETUP);
        }
    }

    private boolean isConfigComplete() {
        File file = new File("config.properties");
        if (!file.exists()) return false;
        try (FileInputStream in = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            String key = props.getProperty("api.key", "").trim();
            return !key.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private void completeSetup() {
        try {
            // Remove old panels
            mainPanel.removeAll();

            // Re-create services from updated config.properties
            cardLayout.removeLayoutComponent(mainPanel);
            initializeServices();
            createAndAddPanels();

            mainPanel.revalidate();
            mainPanel.repaint();
            cardLayout.show(mainPanel, HOME);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(), "Setup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeServices() throws Exception {
        aiService = new AiService();
        alibabaAsrService = new AlibabaAsrService();
        readingService = new ReadingService(aiService, alibabaAsrService);
        conversationService = new ConversationService(aiService, alibabaAsrService);
    }

    private void createAndAddPanels() {
        homePanel = new HomePanel(this::showReading, this::showConversation, this::openSettings);
        ReadingPanel readingPanel = new ReadingPanel(readingService, this::showHome);
        conversationPanel = new ConversationPanel(conversationService, this::showHome);

        mainPanel.add(homePanel, HOME);
        mainPanel.add(readingPanel, READING);
        mainPanel.add(conversationPanel, CONVERSATION);
    }

    private void openSettings() {
        SetupPanel setupPanel = new SetupPanel(() -> {
            try {
                mainPanel.removeAll();
                initializeServices();
                createAndAddPanels();
                mainPanel.revalidate();
                mainPanel.repaint();
                cardLayout.show(mainPanel, HOME);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mainPanel.add(setupPanel, SETUP);
        cardLayout.show(mainPanel, SETUP);
    }

    private void showHome() {
        System.out.println("showHome called, switching to HOME card");
        conversationPanel.reset();
        cardLayout.show(mainPanel, HOME);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showReading() {
        cardLayout.show(mainPanel, READING);
    }

    private void showConversation() {
        cardLayout.show(mainPanel, CONVERSATION);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // FlatLaf 暖色主题
                com.formdev.flatlaf.FlatLightLaf.setup();
                applyWarmTheme();
                new EnglishSpeakerApp().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }

    private static void applyWarmTheme() {
        // 暖色主调：奶油背景 + 深棕文字 + 琥珀色强调
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        // 按钮：暖橙渐变
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.startBackground", new Color(0xF4976C));
        UIManager.put("Button.endBackground", new Color(0xE8734A));
        UIManager.put("Button.hoverBackground", new Color(0xF7A88B));
        // 面板背景：暖奶油
        UIManager.put("Panel.background", new Color(0xFFF8F0));
        // 文本区
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", new Color(0x3D2B1F));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", new Color(0x3D2B1F));
        // 选择色：淡暖
        UIManager.put("TextComponent.selectionBackground", new Color(0xFDDCB5));
    }
}
