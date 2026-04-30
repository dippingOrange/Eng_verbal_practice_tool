package com.englishspeaker.model;

import java.util.List;

public class WordScore {
    private String word;
    private int score;
    private List<PhonemeScore> phonemeScores;

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<PhonemeScore> getPhonemeScores() { return phonemeScores; }
    public void setPhonemeScores(List<PhonemeScore> phonemeScores) { this.phonemeScores = phonemeScores; }
}
