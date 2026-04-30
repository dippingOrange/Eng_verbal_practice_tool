package com.englishspeaker.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SetupPanel extends JPanel {
    private final JTextField endpointField = new JTextField(30);
    private final JPasswordField apiKeyField = new JPasswordField(30);
    private final JTextField aliyunKeyIdField = new JTextField(30);
    private final JPasswordField aliyunKeySecretField = new JPasswordField(30);
    private final JTextField aliyunAppKeyField = new JTextField(30);
    private final JLabel statusLabel = new JLabel(" ");
    private final Runnable onComplete;

    public SetupPanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Title
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        JLabel title = new JLabel("API Configuration");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, gbc);
        gbc.gridwidth = 1;

        // --- DeepSeek Section ---
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2;
        add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = ++y;
        add(createBoldLabel("DeepSeek (required)"), gbc);

        gbc.gridx = 0; gbc.gridy = ++y;
        add(new JLabel("API URL:"), gbc);
        gbc.gridx = 1;
        add(endpointField, gbc);

        gbc.gridx = 0; gbc.gridy = ++y;
        add(new JLabel("API Key:"), gbc);
        gbc.gridx = 1;
        add(apiKeyField, gbc);

        // --- Alibaba ASR Section ---
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2;
        add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = ++y;
        add(createBoldLabel("Alibaba ASR (optional)"), gbc);

        gbc.gridx = 0; gbc.gridy = ++y;
        add(new JLabel("AccessKey ID:"), gbc);
        gbc.gridx = 1;
        add(aliyunKeyIdField, gbc);

        gbc.gridx = 0; gbc.gridy = ++y;
        add(new JLabel("AccessKey Secret:"), gbc);
        gbc.gridx = 1;
        add(aliyunKeySecretField, gbc);

        gbc.gridx = 0; gbc.gridy = ++y;
        add(new JLabel("AppKey:"), gbc);
        gbc.gridx = 1;
        add(aliyunAppKeyField, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = ++y; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton saveBtn = new JButton("Save & Start");
        saveBtn.setPreferredSize(new Dimension(140, 35));
        saveBtn.addActionListener(e -> handleSave());
        btnPanel.add(saveBtn);
        add(btnPanel, gbc);

        // Status
        gbc.gridy = ++y;
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLabel, gbc);

        // Pre-fill from existing config
        loadExistingConfig();
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13));
        return label;
    }

    private void loadExistingConfig() {
        File file = new File("config.properties");
        if (!file.exists()) return;
        try (FileInputStream in = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            setIfPresent(props, "api.endpoint", endpointField);
            setIfPresent(props, "api.key", apiKeyField);
            setIfPresent(props, "aliyun.accessKeyId", aliyunKeyIdField);
            setIfPresent(props, "aliyun.accessKeySecret", aliyunKeySecretField);
            setIfPresent(props, "aliyun.appKey", aliyunAppKeyField);
        } catch (IOException ignored) {}
    }

    private void setIfPresent(Properties props, String key, JTextField field) {
        String val = props.getProperty(key);
        if (val != null && !val.isEmpty()) field.setText(val);
    }

    private void setIfPresent(Properties props, String key, JPasswordField field) {
        String val = props.getProperty(key);
        if (val != null && !val.isEmpty()) field.setText(val);
    }

    private void handleSave() {
        String endpoint = endpointField.getText().trim();
        String apiKey = new String(apiKeyField.getPassword()).trim();

        // DeepSeek 必填
        if (endpoint.isEmpty() || apiKey.isEmpty()) {
            statusLabel.setText("API URL and API Key are required.");
            return;
        }

        try {
            // Load existing or create new
            Properties props = new Properties();
            File file = new File("config.properties");
            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    props.load(in);
                }
            }

            // Update DeepSeek
            props.setProperty("api.endpoint", endpoint);
            props.setProperty("api.key", apiKey);
            if (!props.containsKey("api.model")) {
                props.setProperty("api.model", "deepseek-chat");
            }

            // Update Alibaba ASR
            props.setProperty("aliyun.accessKeyId", aliyunKeyIdField.getText().trim());
            props.setProperty("aliyun.accessKeySecret", new String(aliyunKeySecretField.getPassword()).trim());
            props.setProperty("aliyun.appKey", aliyunAppKeyField.getText().trim());

            try (FileOutputStream out = new FileOutputStream(file)) {
                props.store(out, null);
            }

            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Configuration saved.");
            onComplete.run();

        } catch (IOException ex) {
            statusLabel.setText("Error saving config: " + ex.getMessage());
        }
    }
}
