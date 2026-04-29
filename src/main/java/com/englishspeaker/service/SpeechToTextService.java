package com.englishspeaker.service;

import java.io.*;
import java.nio.file.*;

public class SpeechToTextService {
    private static final String WHISPER_PATH = "STT/whisper-cli.exe";
    private static final String MODEL_PATH = "STT/ggml-base.bin";

    public String transcribe(File wavFile) throws Exception {
        if (!new File(WHISPER_PATH).exists()) {
            throw new RuntimeException("whisper-cli.exe not found at " + WHISPER_PATH);
        }
        if (!new File(MODEL_PATH).exists()) {
            throw new RuntimeException("Model file not found at " + MODEL_PATH);
        }

        // Run whisper-cli
        ProcessBuilder pb = new ProcessBuilder(
                WHISPER_PATH,
                "-f", wavFile.getAbsolutePath(),
                "-m", MODEL_PATH,
                "-l", "en",
                "-otxt",
                "-t", "4"
        );
        pb.directory(new File("."));
        pb.redirectErrorStream(true);

        Process process = pb.start();
        // Consume stdout to avoid buffer deadlock
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) { /* discard */ }
        }
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("whisper-cli exited with code " + exitCode);
        }

        // Read output file (whisper creates record_temp.wav.txt)
        Path outputPath = Path.of(wavFile.getAbsolutePath() + ".txt");
        if (!Files.exists(outputPath)) {
            throw new RuntimeException("Transcription output file not found");
        }

        String text = Files.readString(outputPath).trim();
        Files.deleteIfExists(outputPath); // cleanup

        return text;
    }
}
