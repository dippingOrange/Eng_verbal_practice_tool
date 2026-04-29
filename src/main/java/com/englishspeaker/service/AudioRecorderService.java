package com.englishspeaker.service;

import javax.sound.sampled.*;
import java.io.*;

public class AudioRecorderService {
    private static final float SAMPLE_RATE = 16000;
    private static final int SAMPLE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, SAMPLE_BITS, CHANNELS, true, false);

    private volatile boolean recording = false;
    private TargetDataLine line;
    private Thread recordThread;

    public void startRecording() {
        if (recording) return;

        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                throw new RuntimeException("Microphone not available");
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.start();
            recording = true;

            recordThread = new Thread(() -> {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    while (recording) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    line.stop();
                    line.close();

                    // Save recorded data to temp file
                    byte[] audioData = out.toByteArray();
                    if (audioData.length > 0) {
                        saveWav(audioData, getTempFile());
                    }
                } catch (Exception e) {
                    System.err.println("Recording error: " + e.getMessage());
                }
            });
            recordThread.start();

        } catch (Exception e) {
            throw new RuntimeException("Could not start recording: " + e.getMessage());
        }
    }

    public void stopRecording() {
        recording = false;
    }

    public File getTempFile() {
        return new File("record_temp.wav");
    }

    public boolean isRecording() {
        return recording;
    }

    private void saveWav(byte[] audioData, File file) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream ais = new AudioInputStream(bais, FORMAT, audioData.length / FORMAT.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        }
    }
}
