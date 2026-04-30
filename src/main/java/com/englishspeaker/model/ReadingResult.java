package com.englishspeaker.model;

import java.util.List;

public class ReadingResult {
    private int score;
    private String feedback;
    private String tips;

    // 发音评测字段（-1 表示未获取）
    private int pronunciationScore = -1;
    private int fluencyScore = -1;
    private int completenessScore = -1;
    private List<WordScore> wordScores;

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }

    public int getPronunciationScore() { return pronunciationScore; }
    public void setPronunciationScore(int pronunciationScore) { this.pronunciationScore = pronunciationScore; }
    public int getFluencyScore() { return fluencyScore; }
    public void setFluencyScore(int fluencyScore) { this.fluencyScore = fluencyScore; }
    public int getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(int completenessScore) { this.completenessScore = completenessScore; }
    public List<WordScore> getWordScores() { return wordScores; }
    public void setWordScores(List<WordScore> wordScores) { this.wordScores = wordScores; }
}
