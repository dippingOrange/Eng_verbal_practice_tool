package com.englishspeaker.service;

import com.englishspeaker.model.PronunciationResult;
import com.englishspeaker.model.ReadingResult;
import com.englishspeaker.model.WordScore;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;

public class ReadingService {
    private static final String TEXTS_FILE = "texts.txt";
    private final AiService aiService;
    private final AlibabaAsrService alibabaAsrService;
    private final Gson gson = new Gson();

    public ReadingService(AiService aiService, AlibabaAsrService alibabaAsrService) {
        this.aiService = aiService;
        this.alibabaAsrService = alibabaAsrService;
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

    // 无音频的评估（兼容旧路径）
    public ReadingResult evaluate(String passage, String userInput) throws Exception {
        return evaluate(passage, userInput, null);
    }

    // 带音频的评估（新路径：先用阿里云发音评测，再调 DeepSeek 做教练反馈）
    public ReadingResult evaluate(String passage, String userInput, File wavFile) throws Exception {
        // 1. 发音评测（如果有音频且阿里云可用）
        PronunciationResult pronResult = null;
        if (wavFile != null && wavFile.exists() && alibabaAsrService.isEnabled()) {
            pronResult = alibabaAsrService.evaluate(wavFile, passage);
        }

        // 2. 构建 DeepSeek prompt
        String systemPrompt = buildSystemPrompt(pronResult);
        String userMessage = "Passage: " + passage + "\n\nMy reading: " + userInput;

        // 3. 调用 DeepSeek
        String response = aiService.callApi(systemPrompt, userMessage);

        // 4. 解析 DeepSeek 返回的 JSON
        ReadingResult result = parseAiResponse(response);

        // 5. 合并发音评测数据
        if (pronResult != null) {
            result.setPronunciationScore(pronResult.getPronunciationScore());
            result.setFluencyScore(pronResult.getFluencyScore());
            result.setCompletenessScore(pronResult.getCompletenessScore());
            result.setWordScores(pronResult.getWordScores());
        }

        return result;
    }

    private String buildSystemPrompt(PronunciationResult pronResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an English pronunciation coach.");

        if (pronResult != null) {
            sb.append("\nAcoustic pronunciation assessment (Alibaba Cloud):");
            sb.append("\n- Pronunciation score: ").append(pronResult.getPronunciationScore()).append("/100");
            sb.append("\n- Fluency: ").append(pronResult.getFluencyScore()).append("/100");
            sb.append("\n- Completeness: ").append(pronResult.getCompletenessScore()).append("/100");

            List<WordScore> words = pronResult.getWordScores();
            if (words != null && !words.isEmpty()) {
                sb.append("\n- Words needing attention: ");
                for (WordScore ws : words) {
                    if (ws.getScore() < 80) {
                        sb.append(ws.getWord()).append("(").append(ws.getScore()).append(") ");
                    }
                }
            }
            sb.append("\nUse this objective data to give targeted coaching advice.");
        }

        sb.append(" Return JSON only: {\"score\": <0-100>, \"feedback\": \"...\", \"tips\": \"...\"}");
        return sb.toString();
    }

    private ReadingResult parseAiResponse(String response) {
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
