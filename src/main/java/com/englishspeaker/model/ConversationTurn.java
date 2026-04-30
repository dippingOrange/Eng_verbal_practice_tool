package com.englishspeaker.model;

public class ConversationTurn {
    private String userText;
    private int pronunciationScore = -1;
    private String aiResponse;

    public ConversationTurn(String userText, int pronunciationScore, String aiResponse) {
        this.userText = userText;
        this.pronunciationScore = pronunciationScore;
        this.aiResponse = aiResponse;
    }

    public String getUserText() { return userText; }
    public int getPronunciationScore() { return pronunciationScore; }
    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }
}
