package com.englishspeaker.service;

import com.englishspeaker.model.ConversationTurn;
import com.englishspeaker.model.Scenario;

import java.util.*;

public class ConversationService {
    public static final int LENGTH_NORMAL = 0;
    public static final int LENGTH_DETAILED = 1;
    public static final String[] LENGTH_LABELS = {"Normal", "Detailed"};

    private final AiService aiService;
    private final AlibabaAsrService alibabaAsrService;
    private Scenario currentScenario;
    private int responseLength = LENGTH_NORMAL;
    private final List<ConversationTurn> turns = new ArrayList<>();

    public ConversationService(AiService aiService, AlibabaAsrService alibabaAsrService) {
        this.aiService = aiService;
        this.alibabaAsrService = alibabaAsrService;
    }

    public void setResponseLength(int length) { this.responseLength = length; }
    public int getResponseLength() { return responseLength; }

    public void setScenario(Scenario scenario) {
        this.currentScenario = scenario;
    }

    public Scenario getCurrentScenario() {
        return currentScenario;
    }

    private String buildPrompt() {
        String base = currentScenario.getPrompt();
        if (responseLength == LENGTH_DETAILED) {
            return base + "\nGive detailed responses (4-6 sentences). "
                    + "Ask follow-up questions. Provide rich descriptions and examples.";
        }
        return base + "\nKeep responses concise (2-3 sentences).";
    }

    public String sendMessage(String userInput) throws Exception {
        return sendMessage(userInput, null);
    }

    public String sendMessage(String userInput, java.io.File wavFile) throws Exception {
        int pronScore = -1;

        if (wavFile != null && wavFile.exists() && alibabaAsrService.isEnabled()) {
            var result = alibabaAsrService.evaluate(wavFile, userInput);
            if (result != null) {
                pronScore = result.getPronunciationScore();
            }
        }

        String aiResponse = aiService.callApi(buildPrompt(), userInput);
        turns.add(new ConversationTurn(userInput, pronScore, aiResponse));
        return aiResponse;
    }

    public String startConversation() throws Exception {
        return aiService.callApi(buildPrompt(), currentScenario.getInitialMessage());
    }

    // 获取整场对话的发音总结
    public String getConversationSummary() throws Exception {
        if (turns.isEmpty()) return "No conversation data to summarize.";

        StringBuilder turnData = new StringBuilder();
        int totalScore = 0;
        int scoredTurns = 0;

        for (int i = 0; i < turns.size(); i++) {
            ConversationTurn turn = turns.get(i);
            turnData.append("Turn ").append(i + 1).append(": \"")
                    .append(turn.getUserText()).append("\"");
            if (turn.getPronunciationScore() >= 0) {
                turnData.append(" [Pronunciation: ").append(turn.getPronunciationScore()).append("/100]");
                totalScore += turn.getPronunciationScore();
                scoredTurns++;
            }
            turnData.append("\n  AI: ").append(turn.getAiResponse()).append("\n\n");
        }

        String avgScore = scoredTurns > 0
                ? String.valueOf(totalScore / scoredTurns)
                : "N/A (no voice input)";

        String systemPrompt = "You are an English pronunciation coach. "
                + "A student just completed a conversation practice session. "
                + "Below is the conversation log with pronunciation scores from Alibaba Cloud speech assessment. "
                + "Provide: 1) Overall pronunciation assessment, "
                + "2) 2-3 specific tips for improvement, "
                + "3) An encouraging closing message. "
                + "Keep it 3-5 sentences, conversational and supportive.";

        String userMessage = "Average pronunciation score: " + avgScore + "/100\n\n"
                + "Conversation log:\n" + turnData;

        return aiService.callApi(systemPrompt, userMessage);
    }

    public void resetConversation() {
        turns.clear();
    }

    public boolean hasScoredTurns() {
        return turns.stream().anyMatch(t -> t.getPronunciationScore() >= 0);
    }
}
