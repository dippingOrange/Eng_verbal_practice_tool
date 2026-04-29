package com.englishspeaker.gui;

import com.englishspeaker.model.Scenario;
import com.englishspeaker.service.AudioRecorderService;
import com.englishspeaker.service.ConversationService;
import com.englishspeaker.service.SpeechToTextService;

import javax.swing.*;
import java.awt.*;

public class ConversationPanel extends JPanel {
    private final JComboBox<Scenario> scenarioBox = new JComboBox<>(Scenario.values());
    private final JTextArea chatArea = new JTextArea(15, 50);
    private final JTextField inputField = new JTextField(40);
    private final JButton sendBtn = new JButton("Send");
    private final JButton startBtn = new JButton("Start Conversation");
    private final JButton backBtn = new JButton("Back");
    private final JButton recordBtn = new JButton("🎤 Record");
    private final ConversationService conversationService;
    private final AudioRecorderService recorder = new AudioRecorderService();
    private final SpeechToTextService stt = new SpeechToTextService();
    private boolean conversationActive = false;

    public ConversationPanel(ConversationService conversationService, Runnable onBack) {
        this.conversationService = conversationService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: scenario selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Scenario:"));
        scenarioBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Scenario s) {
                    setText(s.getLabel());
                }
                return this;
            }
        });
        topPanel.add(scenarioBox);
        topPanel.add(startBtn);
        add(topPanel, BorderLayout.NORTH);

        // Center: chat area
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom: input + buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("You:"));
        inputPanel.add(inputField);
        inputPanel.add(sendBtn);
        inputPanel.add(recordBtn);
        recordBtn.addActionListener(e -> handleRecord());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.add(backBtn);
        bottomPanel.add(navPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        startBtn.addActionListener(e -> handleStart());
        sendBtn.addActionListener(e -> handleSend());
        inputField.addActionListener(e -> handleSend());

        setInputEnabled(false);
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendBtn.setEnabled(enabled);
        recordBtn.setEnabled(enabled);
        inputField.setEditable(enabled);
    }

    private void handleRecord() {
        if (recorder.isRecording()) {
            recorder.stopRecording();
            recordBtn.setText("🎤 Transcribing...");
            recordBtn.setEnabled(false);

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return stt.transcribe(recorder.getTempFile());
                }

                @Override
                protected void done() {
                    try {
                        String text = get();
                        inputField.setText(text);
                        inputField.requestFocus();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ConversationPanel.this,
                                "Transcription failed: " + e.getMessage());
                    } finally {
                        recordBtn.setText("🎤 Record");
                        recordBtn.setEnabled(true);
                    }
                }
            };
            worker.execute();
        } else {
            recorder.startRecording();
            recordBtn.setText("🔴 Stop");
        }
    }

    private void handleStart() {
        Scenario scenario = (Scenario) scenarioBox.getSelectedItem();
        conversationService.setScenario(scenario);
        conversationActive = true;
        chatArea.setText("");
        scenarioBox.setEnabled(false);
        startBtn.setEnabled(false);
        chatArea.append("AI: Starting conversation...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return conversationService.startConversation();
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    chatArea.append("AI: " + response + "\n\n");
                    setInputEnabled(true);
                    inputField.requestFocus();
                } catch (Exception e) {
                    chatArea.append("Error: " + e.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }

    private void handleSend() {
        String input = inputField.getText().trim();
        if (input.isEmpty() || !conversationActive) return;
        inputField.setText("");
        chatArea.append("You: " + input + "\n");
        setInputEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return conversationService.sendMessage(input);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    chatArea.append("AI: " + response + "\n\n");
                } catch (Exception e) {
                    chatArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    setInputEnabled(true);
                    inputField.requestFocus();
                }
            }
        };
        worker.execute();
    }

    public void reset() {
        conversationActive = false;
        scenarioBox.setEnabled(true);
        startBtn.setEnabled(true);
        setInputEnabled(false);
        chatArea.setText("");
        inputField.setText("");
    }
}
