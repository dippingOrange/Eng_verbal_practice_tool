package com.englishspeaker.gui;

import com.englishspeaker.model.ReadingResult;
import com.englishspeaker.service.AudioRecorderService;
import com.englishspeaker.service.ReadingService;
import com.englishspeaker.service.SpeechToTextService;
import com.englishspeaker.service.TextToSpeechService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReadingPanel extends JPanel {
    private final JTextArea passageArea = new JTextArea(6, 50);
    private final JTextArea inputArea = new JTextArea(4, 50);
    private final JTextArea resultArea = new JTextArea(5, 50);
    private final JTextArea pronDetailArea = new JTextArea(3, 50);
    private final JButton evaluateBtn = new JButton("Evaluate");
    private final JButton backBtn = new JButton("Back");
    private final ReadingService readingService;
    private final AudioRecorderService recorder = new AudioRecorderService();
    private final SpeechToTextService stt = new SpeechToTextService();
    private final TextToSpeechService tts = new TextToSpeechService();
    private final JButton recordBtn = new JButton("🎤 Record");
    private final JButton ttsToggle = new JButton("🔊 TTS");
    private final JButton replayBtn = new JButton("🔁 Replay");
    private String currentPassage;
    private String lastSpokenText;
    private File lastRecordingFile;

    public ReadingPanel(ReadingService readingService, Runnable onBack) {
        this.readingService = readingService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: passage display
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Read the following passage:"), BorderLayout.NORTH);
        passageArea.setEditable(false);
        passageArea.setLineWrap(true);
        passageArea.setWrapStyleWord(true);
        passageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(new JScrollPane(passageArea), BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center: input + result
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(new JLabel("Type or record the passage:"), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        pronDetailArea.setEditable(false);
        pronDetailArea.setFont(new Font("Arial", Font.PLAIN, 12));
        pronDetailArea.setVisible(false);
        pronDetailArea.setBackground(new Color(245, 245, 255));
        centerPanel.add(new JScrollPane(pronDetailArea), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // TTS controls row (separate from bottom action buttons)
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
        replayBtn.setEnabled(false);
        replayBtn.addActionListener(e -> { if (lastSpokenText != null) tts.speakAsync(lastSpokenText); });
        ttsRow.add(replayBtn);

        // Bottom: action buttons only
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        recordBtn.addActionListener(e -> handleRecord());
        bottomPanel.add(recordBtn);
        JButton newBtn = new JButton("New Passage");
        newBtn.addActionListener(e -> loadNewPassage());
        bottomPanel.add(newBtn);
        evaluateBtn.addActionListener(e -> handleEvaluate());
        bottomPanel.add(evaluateBtn);
        backBtn.addActionListener(e -> onBack.run());
        bottomPanel.add(backBtn);

        // Wrap TTS row + bottom panel together
        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.add(ttsRow, BorderLayout.NORTH);
        southWrap.add(bottomPanel, BorderLayout.SOUTH);
        add(southWrap, BorderLayout.SOUTH);

        loadNewPassage();
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
                        inputArea.setText(text);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ReadingPanel.this,
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
            recordBtn.setForeground(Color.RED);
        }
    }

    private void loadNewPassage() {
        currentPassage = readingService.getRandomPassage();
        if (currentPassage != null) {
            passageArea.setText(currentPassage);
            inputArea.setText("");
            resultArea.setText("");
        } else {
            passageArea.setText("No texts found in texts.txt");
        }
    }

    private void handleEvaluate() {
        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please type the passage first.");
            return;
        }
        evaluateBtn.setEnabled(false);
        resultArea.setText("Evaluating...");

        File wav = (lastRecordingFile != null && lastRecordingFile.exists()) ? lastRecordingFile : null;

        SwingWorker<ReadingResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ReadingResult doInBackground() throws Exception {
                return readingService.evaluate(currentPassage, input, wav);
            }

            @Override
            protected void done() {
                try {
                    ReadingResult result = get();
                    StringBuilder resultText = new StringBuilder();
                    resultText.append("Score: ").append(result.getScore()).append("/100\n");
                    resultText.append("Feedback: ").append(result.getFeedback()).append("\n");
                    resultText.append("Tips: ").append(result.getTips());
                    resultArea.setText(resultText.toString());

                    // 显示发音评测详情
                    if (result.getPronunciationScore() > 0) {
                        pronDetailArea.setText("Pronunciation: " + result.getPronunciationScore() + "/100  "
                                + "Fluency: " + result.getFluencyScore() + "/100  "
                                + "Completeness: " + result.getCompletenessScore() + "/100");
                        pronDetailArea.setVisible(true);
                    } else {
                        pronDetailArea.setVisible(false);
                    }

                    lastSpokenText = "Your score is " + result.getScore()
                            + " out of 100. " + result.getFeedback()
                            + ". Tip: " + result.getTips();
                    replayBtn.setEnabled(true);
                    tts.speakAsync(lastSpokenText);
                } catch (Exception e) {
                    resultArea.setText("Error: " + e.getMessage());
                } finally {
                    evaluateBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
