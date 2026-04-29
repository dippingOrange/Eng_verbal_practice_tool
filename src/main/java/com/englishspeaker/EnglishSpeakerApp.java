package com.englishspeaker;

import com.englishspeaker.gui.*;
import com.englishspeaker.service.*;

import javax.swing.*;
import java.awt.*;

public class EnglishSpeakerApp extends JFrame {
    private static final String LOGIN = "LOGIN";
    private static final String HOME = "HOME";
    private static final String READING = "READING";
    private static final String CONVERSATION = "CONVERSATION";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final UserService userService;
    private final AiService aiService;
    private final HomePanel homePanel;
    private final ConversationPanel conversationPanel;

    public EnglishSpeakerApp() throws Exception {
        setTitle("English Speaking Practice");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        userService = new UserService();
        aiService = new AiService();
        ReadingService readingService = new ReadingService(aiService);
        ConversationService conversationService = new ConversationService(aiService);

        // Create panels
        LoginPanel loginPanel = new LoginPanel(userService, this::showHome);
        homePanel = new HomePanel(userService, this::showReading, this::showConversation, this::showLogin);
        ReadingPanel readingPanel = new ReadingPanel(readingService, this::showHome);
        conversationPanel = new ConversationPanel(conversationService, this::showHome);

        mainPanel.add(loginPanel, LOGIN);
        mainPanel.add(homePanel, HOME);
        mainPanel.add(readingPanel, READING);
        mainPanel.add(conversationPanel, CONVERSATION);

        add(mainPanel);
        cardLayout.show(mainPanel, LOGIN);
    }

    private void showLogin() {
        homePanel.updateWelcome(userService.getCurrentUser());
        cardLayout.show(mainPanel, LOGIN);
    }

    private void showHome() {
        conversationPanel.reset();
        cardLayout.show(mainPanel, HOME);
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
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new EnglishSpeakerApp().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}
