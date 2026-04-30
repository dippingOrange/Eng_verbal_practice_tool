package com.englishspeaker.service;

import java.io.*;
import java.util.*;

public class TextToSpeechService {
    private boolean enabled = true;
    private String voice = "en-US-AriaNeural";

    public static final Map<String, String> VOICES = new LinkedHashMap<>();
    static {
        VOICES.put("Aria (女声, 自信)", "en-US-AriaNeural");
        VOICES.put("Jenny (女声, 友善)", "en-US-JennyNeural");
        VOICES.put("Emma (女声, 轻快)", "en-US-EmmaMultilingualNeural");
        VOICES.put("Guy (男声, 热情)", "en-US-GuyNeural");
        VOICES.put("Christopher (男声, 权威)", "en-US-ChristopherNeural");
        VOICES.put("Andrew (男声, 温暖)", "en-US-AndrewMultilingualNeural");
        VOICES.put("Brian (男声, 随意)", "en-US-BrianMultilingualNeural");
    }

    public void setVoice(String voice) { this.voice = voice; }
    public String getVoice() { return voice; }
    public String[] getVoiceNames() { return VOICES.keySet().toArray(new String[0]); }

    private double speed = 1.0;
    public static final double[] SPEEDS = {0.5, 0.75, 1.0, 1.25, 1.5};
    public static final String[] SPEED_LABELS = {"0.5x", "0.75x", "1.0x", "1.25x", "1.5x"};

    public void setSpeed(double speed) { this.speed = speed; }
    public double getSpeed() { return speed; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void speak(String text) {
        if (!enabled || text == null || text.isEmpty()) return;
        if (text.length() > 500) text = text.substring(0, 497) + "...";

        File tempFile = new File("tts_temp.mp3");
        tempFile.delete();

        try {
            // Step 1: 生成完整音频文件（不做流式播放）
            List<String> genCmd = new ArrayList<>();
            genCmd.add("edge-tts");
            genCmd.add("--voice"); genCmd.add(voice);
            if (speed != 1.0) {
                int ratePercent = Math.round((float) ((speed - 1.0) * 100));
                genCmd.add("--rate");
                genCmd.add((ratePercent >= 0 ? "+" : "") + ratePercent + "%");
            }
            genCmd.add("--text"); genCmd.add(text);
            genCmd.add("--write-media"); genCmd.add(tempFile.getAbsolutePath());

            Process gen = new ProcessBuilder(genCmd).redirectErrorStream(true).start();
            gen.getInputStream().transferTo(OutputStream.nullOutputStream());
            int code = gen.waitFor();

            if (code != 0 || !tempFile.exists() || tempFile.length() == 0) return;

            // Step 2: 播放完整文件（从头开始，零丢词）
            new ProcessBuilder("cmd", "/c", "start", "/min", "wmplayer",
                    tempFile.getAbsolutePath(), "/close").start();

            // 等待 wmplayer 播放完毕（最长 60s）
            Thread.sleep(Math.min(60000, tempFile.length() / 2000 + 2000));
            tempFile.delete();

        } catch (IOException | InterruptedException e) {
            // silently ignore
        }
    }

    public void speakAsync(String text) {
        new Thread(() -> speak(text)).start();
    }
}
