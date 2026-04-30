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
        // 前导停顿：消化 edge-playback 初始化延迟，避免开头丢词
        text = "... " + text;
        if (text.length() > 500) text = text.substring(0, 497) + "...";

        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("edge-playback");
            cmd.add("--voice"); cmd.add(voice);
            if (speed != 1.0) {
                int ratePercent = Math.round((float) ((speed - 1.0) * 100));
                cmd.add("--rate");
                cmd.add((ratePercent >= 0 ? "+" : "") + ratePercent + "%");
            }
            cmd.add("--text"); cmd.add(text);

            new ProcessBuilder(cmd).redirectErrorStream(true).start().waitFor();

        } catch (IOException | InterruptedException e) {
            // silently ignore — user can retry with replay button
        }
    }

    public void speakAsync(String text) {
        new Thread(() -> speak(text)).start();
    }
}
