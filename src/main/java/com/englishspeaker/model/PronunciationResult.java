package com.englishspeaker.model;

import java.util.List;

public class PronunciationResult {
    private int overallScore;
    private int pronunciationScore;
    private int fluencyScore;
    private int completenessScore;
    private List<WordScore> wordScores;

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public int getPronunciationScore() { return pronunciationScore; }
    public void setPronunciationScore(int pronunciationScore) { this.pronunciationScore = pronunciationScore; }
    public int getFluencyScore() { return fluencyScore; }
    public void setFluencyScore(int fluencyScore) { this.fluencyScore = fluencyScore; }
    public int getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(int completenessScore) { this.completenessScore = completenessScore; }
    public List<WordScore> getWordScores() { return wordScores; }
    public void setWordScores(List<WordScore> wordScores) { this.wordScores = wordScores; }
}
