package com.englishspeaker.gui;

import com.englishspeaker.service.UserService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel statusLabel = new JLabel(" ");
    private final UserService userService;
    private final Runnable onLoginSuccess;

    public LoginPanel(UserService userService, Runnable onLoginSuccess) {
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("English Speaking Practice");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> handleRegister());
        add(registerBtn, gbc);

        gbc.gridx = 1;
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        add(loginBtn, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        statusLabel.setForeground(Color.RED);
        add(statusLabel, gbc);
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (userService.register(username, password)) {
            statusLabel.setForeground(new Color(0, 128, 0));
            statusLabel.setText("Registration successful! Please login.");
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Registration failed. Username may already exist.");
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (userService.login(username, password)) {
            statusLabel.setText(" ");
            onLoginSuccess.run();
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Invalid username or password.");
        }
    }
}
