package com.englishspeaker.gui;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {

    public HomePanel(Runnable onReading, Runnable onConversation, Runnable onSettings) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        JLabel titleLabel = new JLabel("English Speaking Practice");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy = 0;
        add(titleLabel, gbc);

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

        JButton settingsBtn = new JButton("API Settings");
        settingsBtn.setPreferredSize(new Dimension(220, 30));
        settingsBtn.addActionListener(e -> onSettings.run());
        gbc.gridy = 3;
        add(settingsBtn, gbc);
    }
}
