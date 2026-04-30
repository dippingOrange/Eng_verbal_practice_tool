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
    private final JButton hideTextBtn = new JButton("🙈 Hide Text");
    private final ConversationService conversationService;
    private final AudioRecorderService recorder = new AudioRecorderService();
    private final SpeechToTextService stt = new SpeechToTextService();
    private final TextToSpeechService tts = new TextToSpeechService();
    private boolean conversationActive = false;
    private String lastAiResponse;
    private File lastRecordingFile;

    // 加载动画
    private final String[] SPINNER = {"|", "/", "—", "\\"};
    private int spinnerIdx = 0;
    private Timer spinnerTimer;
    private int loadingMark; // chatArea 中 "AI: " 标记位置，用于替换 spinner

    private void showLoading() {
        loadingMark = chatArea.getDocument().getLength();
        chatArea.append("AI: |");
        spinnerIdx = 0;
        if (spinnerTimer == null) {
            spinnerTimer = new Timer(200, e -> {
                spinnerIdx = (spinnerIdx + 1) % SPINNER.length;
                try {
                    chatArea.replaceRange(SPINNER[spinnerIdx], loadingMark + 4, loadingMark + 5);
                } catch (Exception ignored) {}
            });
        }
        spinnerTimer.start();
    }

    private Timer typewriterTimer;
    private int typePos;
    private String typeText;

    private void hideLoading(String response) {
        if (spinnerTimer != null) spinnerTimer.stop();
        // 清除 loading 标记，开始打字机效果
        try {
            chatArea.replaceRange("", loadingMark, chatArea.getDocument().getLength());
        } catch (Exception ignored) {}
        typewrite(response);
    }

    private void typewrite(String text) {
        typeText = text;
        typePos = 0;

        // 先写入 "AI: " 前缀
        if (!chatArea.getText().endsWith("\n") && chatArea.getDocument().getLength() > 0) {
            chatArea.append("\n");
        }
        chatArea.append("AI: ");
        loadingMark = chatArea.getDocument().getLength();

        if (typewriterTimer != null) typewriterTimer.stop();
        typewriterTimer = new Timer(25, e -> {
            if (typePos < typeText.length()) {
                chatArea.append(String.valueOf(typeText.charAt(typePos)));
                typePos++;
            } else {
                typewriterTimer.stop();
                chatArea.append("\n\n");
            }
        });
        typewriterTimer.start();
    }

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

        hideTextBtn.addActionListener(e -> {
            if (chatArea.getForeground().equals(Color.WHITE)) {
                chatArea.setForeground(Color.BLACK);
                hideTextBtn.setText("🙈 Hide Text");
            } else {
                chatArea.setForeground(Color.WHITE);
                hideTextBtn.setText("🙉 Show Text");
            }
        });
        ttsRow.add(hideTextBtn);
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
        showLoading();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return conversationService.startConversation();
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    hideLoading(response);
                    lastAiResponse = response;
                    replayBtn.setEnabled(true);
                    tts.speakAsync(response);
                    setInputEnabled(true);
                    inputField.requestFocus();
                } catch (Exception e) {
                    hideLoading("Error: " + e.getMessage());
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
        showLoading();
        setInputEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return conversationService.sendMessage(input, wav);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (wav != null && conversationService.hasScoredTurns()) summaryBtn.setEnabled(true);
                    hideLoading(response);
                    lastAiResponse = response;
                    replayBtn.setEnabled(true);
                    tts.speakAsync(response);
                } catch (Exception e) {
                    hideLoading("Error: " + e.getMessage());
                } finally {
                    setInputEnabled(true);
                    inputField.requestFocus();
                }
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
