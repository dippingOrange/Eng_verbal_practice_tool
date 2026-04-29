package com.englishspeaker.model;

public enum Scenario {
    INTERVIEW("面试", "你是一位面试官，用英语进行技术面试，每次问一个问题，等用户回答后再继续"),
    FRIEND_CHAT("朋友聊天", "你是一位友善的英语母语者，和用户聊日常话题，像朋友一样自然对话"),
    ASKING_HELP("求助", "你是商店店员，顾客（用户）需要帮助，用清晰简单的英语提供帮助");

    private final String label;
    private final String prompt;

    Scenario(String label, String prompt) {
        this.label = label;
        this.prompt = prompt;
    }

    public String getLabel() { return label; }
    public String getPrompt() { return prompt; }

    public String getInitialMessage() {
        return switch (this) {
            case INTERVIEW -> "I'm ready for the interview. Please start.";
            case FRIEND_CHAT -> "Hi! How are you doing today?";
            case ASKING_HELP -> "Hello, I need some help please.";
        };
    }
}
