package com.englishspeaker.service;

import java.io.*;
import java.util.*;

public class TextToSpeechService {
    private boolean enabled = true;
    private String voice = "en-US-AriaNeural";

    // 推荐英文语音列表
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

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getVoice() {
        return voice;
    }

    public String[] getVoiceNames() {
        return VOICES.keySet().toArray(new String[0]);
    }

    private double speed = 1.0;

    public static final double[] SPEEDS = {0.5, 0.75, 1.0, 1.25, 1.5};
    public static final String[] SPEED_LABELS = {"0.5x (很慢)", "0.75x (较慢)", "1.0x (正常)", "1.25x (较快)", "1.5x (很快)"};

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void speak(String text) {
        if (!enabled || text == null || text.isEmpty()) return;

        // Limit length
        if (text.length() > 500) {
            text = text.substring(0, 497) + "...";
        }

        File tempFile = new File("tts_temp.mp3");
        try {
            // Step 1: Generate full audio file first (buffers completely before playback)
            List<String> cmd = new ArrayList<>();
            cmd.add("edge-tts");
            cmd.add("--voice");
            cmd.add(voice);
            if (speed != 1.0) {
                int ratePercent = Math.round((float) ((speed - 1.0) * 100));
                cmd.add("--rate");
                cmd.add((ratePercent >= 0 ? "+" : "") + ratePercent + "%");
            }
            cmd.add("--text");
            cmd.add(text);
            cmd.add("--write-media");
            cmd.add(tempFile.getAbsolutePath());

            ProcessBuilder gen = new ProcessBuilder(cmd);
            gen.redirectErrorStream(true);
            Process genProcess = gen.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(genProcess.getInputStream()))) {
                while (reader.readLine() != null) { /* discard */ }
            }
            int exitCode = genProcess.waitFor();
            if (exitCode != 0 || !tempFile.exists()) {
                System.err.println("TTS generation failed with code " + exitCode);
                return;
            }

            // Step 2: Play using Windows Media Player COM (background, no window)
            String absPath = tempFile.getAbsolutePath();
            String psCommand = "$wp = New-Object -ComObject 'WMPlayer.OCX'; "
                    + "$wp.URL = '" + absPath + "'; "
                    + "$wp.controls.play(); "
                    + "while ($wp.playState -ne 1) { Start-Sleep -Milliseconds 200 }; "
                    + "$wp = $null";

            ProcessBuilder play = new ProcessBuilder("powershell.exe", "-Command", psCommand);
            play.redirectErrorStream(true);
            Process playProcess = play.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(playProcess.getInputStream()))) {
                while (reader.readLine() != null) { /* discard */ }
            }
            playProcess.waitFor();

            // Cleanup
            tempFile.delete();

        } catch (IOException e) {
            System.err.println("TTS error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void speakAsync(String text) {
        new Thread(() -> speak(text)).start();
    }
}
