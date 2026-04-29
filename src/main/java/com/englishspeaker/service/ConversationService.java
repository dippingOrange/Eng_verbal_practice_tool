package com.englishspeaker.service;

import com.englishspeaker.model.Scenario;

public class ConversationService {
    private final AiService aiService;
    private Scenario currentScenario;

    public ConversationService(AiService aiService) {
        this.aiService = aiService;
    }

    public void setScenario(Scenario scenario) {
        this.currentScenario = scenario;
    }

    public Scenario getCurrentScenario() {
        return currentScenario;
    }

    public String startConversation() throws Exception {
        String systemPrompt = currentScenario.getPrompt()
                + "\nKeep responses concise (2-3 sentences).";
        return aiService.callApi(systemPrompt, currentScenario.getInitialMessage());
    }

    public String sendMessage(String userInput) throws Exception {
        String systemPrompt = currentScenario.getPrompt()
                + "\nKeep responses concise (2-3 sentences).";
        return aiService.callApi(systemPrompt, userInput);
    }
}
