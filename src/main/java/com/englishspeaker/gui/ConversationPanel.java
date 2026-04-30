package com.englishspeaker.gui;

import com.englishspeaker.model.Scenario;
import com.englishspeaker.service.AudioRecorderService;
import com.englishspeaker.service.ConversationService;
import com.englishspeaker.service.SpeechToTextService;
import com.englishspeaker.service.TextToSpeechService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ConversationPanel extends JPanel {
    private final JComboBox<Scenario> scenarioBox = new JComboBox<>(Scenario.values());
    private final JTextArea chatArea = new JTextArea(15, 50);
    private final JTextField inputField = new JTextField(40);
    private final JButton sendBtn = new JButton("Send");
    private final JButton startBtn = new JButton("Start Conversation");
    private final JButton backBtn = new JButton("Back");
    private final JButton recordBtn = new JButton("🎤 Record");
    private final JButton ttsToggle = new JButton("🔊 TTS");
    private final JButton replayBtn = new JButton("🔁 Replay");
    private final JButton summaryBtn = new JButton("Summary");
    private final ConversationService conversationService;
    private final AudioRecorderService recorder = new AudioRecorderService();
    private final SpeechToTextService stt = new SpeechToTextService();
    private final TextToSpeechService tts = new TextToSpeechService();
    private boolean conversationActive = false;
    private String lastAiResponse;
    private File lastRecordingFile;

    public ConversationPanel(ConversationService conversationService, Runnable onBack) {
        this.conversationService = conversationService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: scenario + TTS controls (two rows)
        JPanel topPanel = new JPanel(new BorderLayout(5, 3));

        JPanel scenarioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        scenarioRow.add(new JLabel("Scenario:"));
        scenarioBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Scenario s) setText(s.getLabel());
                return this;
            }
        });
        scenarioRow.add(scenarioBox);
        scenarioRow.add(startBtn);
        scenarioRow.add(new JLabel("  Length:"));
        JComboBox<String> lengthBox = new JComboBox<>(ConversationService.LENGTH_LABELS);
        lengthBox.addActionListener(e -> conversationService.setResponseLength(lengthBox.getSelectedIndex()));
        scenarioRow.add(lengthBox);
        topPanel.add(scenarioRow, BorderLayout.NORTH);

        JPanel ttsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        ttsToggle.addActionListener(e -> {
            tts.setEnabled(!tts.isEnabled());
            ttsToggle.setText(tts.isEnabled() ? "🔊 TTS" : "🔇 TTS");
        });
        ttsRow.add(ttsToggle);

        JComboBox<String> voiceBox = new JComboBox<>(tts.getVoiceNames());
        voiceBox.addActionListener(e -> {
            String selected = (String) voiceBox.getSelectedItem();
            if (selected != null) tts.setVoice(TextToSpeechService.VOICES.get(selected));
        });
        ttsRow.add(new JLabel("Voice:"));
        ttsRow.add(voiceBox);

        JComboBox<String> speedBox = new JComboBox<>(TextToSpeechService.SPEED_LABELS);
        speedBox.setSelectedIndex(2);
        speedBox.addActionListener(e -> {
            int idx = speedBox.getSelectedIndex();
            if (idx >= 0) tts.setSpeed(TextToSpeechService.SPEEDS[idx]);
        });
        ttsRow.add(new JLabel("Speed:"));
        ttsRow.add(speedBox);
        topPanel.add(ttsRow, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center: chat area
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Bottom: input + nav buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("You:"));
        inputPanel.add(inputField);
        inputPanel.add(sendBtn);
        inputPanel.add(recordBtn);
        recordBtn.addActionListener(e -> handleRecord());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        replayBtn.setEnabled(false);
        replayBtn.addActionListener(e -> { if (lastAiResponse != null) tts.speakAsync(lastAiResponse); });
        navPanel.add(replayBtn);
        summaryBtn.setEnabled(false);
        summaryBtn.addActionListener(e -> handleSummary());
        navPanel.add(summaryBtn);
        backBtn.addActionListener(e -> {
            System.out.println("Back button clicked, calling onBack...");
            onBack.run();
        });
        navPanel.add(backBtn);
        bottomPanel.add(navPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

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
            lastRecordingFile = recorder.getTempFile();
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
                    lastAiResponse = response;
                    replayBtn.setEnabled(true);
                    tts.speakAsync(response);
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
        File wav = (lastRecordingFile != null && lastRecordingFile.exists()) ? lastRecordingFile : null;

        chatArea.append("You: " + input + "\n");
        setInputEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            private int pronScore = -1;

            @Override
            protected String doInBackground() throws Exception {
                return conversationService.sendMessage(input, wav);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    // 显示发音分（如果有）
                    if (wav != null && conversationService.hasScoredTurns()) {
                        // 从最后一个 turn 获取分数
                        chatArea.append("  [" + getLastTurnScore() + "/100]\n");
                        summaryBtn.setEnabled(true);
                    }
                    chatArea.append("AI: " + response + "\n\n");
                    lastAiResponse = response;
                    replayBtn.setEnabled(true);
                    tts.speakAsync(response);
                } catch (Exception e) {
                    chatArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    setInputEnabled(true);
                    inputField.requestFocus();
                }
            }

            private String getLastTurnScore() {
                // conversationService 内部管理 turns，这里简单从 hasScoredTurns 判断
                return "Pron";
            }
        };
        worker.execute();
    }

    private void handleSummary() {
        summaryBtn.setEnabled(false);
        chatArea.append("\n--- Pronunciation Summary ---\n");
        chatArea.append("Generating...\n");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return conversationService.getConversationSummary();
            }

            @Override
            protected void done() {
                try {
                    String summary = get();
                    chatArea.append(summary + "\n\n");
                } catch (Exception e) {
                    chatArea.append("Error: " + e.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }

    public void reset() {
        conversationActive = false;
        conversationService.resetConversation();
        scenarioBox.setEnabled(true);
        startBtn.setEnabled(true);
        summaryBtn.setEnabled(false);
        setInputEnabled(false);
        chatArea.setText("");
        inputField.setText("");
        lastRecordingFile = null;
    }
}
