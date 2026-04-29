package com.englishspeaker.gui;

import com.englishspeaker.service.UserService;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
    private final JLabel welcomeLabel;

    public HomePanel(UserService userService, Runnable onReading, Runnable onConversation, Runnable onLogout) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        welcomeLabel = new JLabel("Welcome, " + userService.getCurrentUser());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy = 0;
        add(welcomeLabel, gbc);

        JButton readingBtn = new JButton("Reading Test");
        readingBtn.setPreferredSize(new Dimension(220, 40));
        readingBtn.addActionListener(e -> onReading.run());
        gbc.gridy = 1;
        add(readingBtn, gbc);

        JButton conversationBtn = new JButton("Conversation Practice");
        conversationBtn.setPreferredSize(new Dimension(220, 40));
        conversationBtn.addActionListener(e -> onConversation.run());
        gbc.gridy = 2;
        add(conversationBtn, gbc);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(220, 40));
        logoutBtn.addActionListener(e -> {
            userService.logout();
            onLogout.run();
        });
        gbc.gridy = 3;
        add(logoutBtn, gbc);
    }

    public void updateWelcome(String username) {
        welcomeLabel.setText("Welcome, " + username);
    }
}
