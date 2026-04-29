package com.englishspeaker.service;

import com.englishspeaker.model.ReadingResult;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;

public class ReadingService {
    private static final String TEXTS_FILE = "texts.txt";
    private final AiService aiService;
    private final Gson gson = new Gson();

    public ReadingService(AiService aiService) {
        this.aiService = aiService;
    }

    public String getRandomPassage() {
        List<String> passages = new ArrayList<>();
        File file = new File(TEXTS_FILE);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            StringBuilder passage = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (passage.length() > 0) {
                        passages.add(passage.toString().trim());
                        passage.setLength(0);
                    }
                } else {
                    passage.append(line).append(" ");
                }
            }
            if (passage.length() > 0) {
                passages.add(passage.toString().trim());
            }
        } catch (IOException e) {
            return null;
        }
        if (passages.isEmpty()) return null;
        return passages.get(new Random().nextInt(passages.size()));
    }

    public ReadingResult evaluate(String passage, String userInput) throws Exception {
        String systemPrompt = "You are an English pronunciation coach. Evaluate the user's reading and return JSON only: " +
                "{\"score\": <0-100>, \"feedback\": \"...\", \"tips\": \"...\"}";
        String userMessage = "Passage: " + passage + "\n\nMy reading: " + userInput;

        String response = aiService.callApi(systemPrompt, userMessage);

        // 从 AI 回复中提取 JSON
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start != -1 && end > start) {
            String json = response.substring(start, end + 1);
            try {
                return gson.fromJson(json, ReadingResult.class);
            } catch (Exception e) {
                ReadingResult fallback = new ReadingResult();
                fallback.setScore(0);
                fallback.setFeedback(response);
                fallback.setTips("Could not parse structured result.");
                return fallback;
            }
        }
        ReadingResult fallback = new ReadingResult();
        fallback.setScore(0);
        fallback.setFeedback(response);
        fallback.setTips("Could not parse structured result.");
        return fallback;
    }
}
