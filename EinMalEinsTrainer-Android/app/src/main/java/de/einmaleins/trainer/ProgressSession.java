package de.einmaleins.trainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressSession {
    private String id;
    private long startTimestamp;
    private long endTimestamp;
    private int correctAnswers;
    private int wrongAnswers;
    private int totalAttempts;
    
    private HashMap<Integer, Integer> multCorrect;
    private HashMap<Integer, Integer> multWrong;
    private HashMap<Integer, Integer> divCorrect;
    private HashMap<Integer, Integer> divWrong;

    public ProgressSession() {
        this.id = UUID.randomUUID().toString();
        this.startTimestamp = System.currentTimeMillis();
        this.endTimestamp = 0;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.totalAttempts = 0;
        this.multCorrect = new HashMap<>();
        this.multWrong = new HashMap<>();
        this.divCorrect = new HashMap<>();
        this.divWrong = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            multCorrect.put(i, 0);
            multWrong.put(i, 0);
            divCorrect.put(i, 0);
            divWrong.put(i, 0);
        }
    }

    public ProgressSession(int correct, int wrong, int total) {
        this.id = UUID.randomUUID().toString();
        this.startTimestamp = System.currentTimeMillis();
        this.endTimestamp = System.currentTimeMillis();
        this.correctAnswers = correct;
        this.wrongAnswers = wrong;
        this.totalAttempts = total;
        this.multCorrect = new HashMap<>();
        this.multWrong = new HashMap<>();
        this.divCorrect = new HashMap<>();
        this.divWrong = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            multCorrect.put(i, 0);
            multWrong.put(i, 0);
            divCorrect.put(i, 0);
            divWrong.put(i, 0);
        }
    }

    public String getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setCorrectAnswers(int correct) {
        this.correctAnswers = correct;
    }

    public void setWrongAnswers(int wrong) {
        this.wrongAnswers = wrong;
    }

    public void setTotalAttempts(int total) {
        this.totalAttempts = total;
    }

    public double getAccuracy() {
        return totalAttempts > 0 ? (double) correctAnswers / totalAttempts * 100 : 0;
    }

    public HashMap<Integer, Integer> getMultCorrect() {
        return multCorrect;
    }

    public HashMap<Integer, Integer> getMultWrong() {
        return multWrong;
    }

    public HashMap<Integer, Integer> getDivCorrect() {
        return divCorrect;
    }

    public HashMap<Integer, Integer> getDivWrong() {
        return divWrong;
    }

    public int getMultCorrectForSeries(int series) {
        Integer val = multCorrect.get(series);
        return val != null ? val : 0;
    }

    public int getMultWrongForSeries(int series) {
        Integer val = multWrong.get(series);
        return val != null ? val : 0;
    }

    public int getDivCorrectForSeries(int series) {
        Integer val = divCorrect.get(series);
        return val != null ? val : 0;
    }

    public int getDivWrongForSeries(int series) {
        Integer val = divWrong.get(series);
        return val != null ? val : 0;
    }

    public void setSeriesStats(HashMap<Integer, Integer> multC, HashMap<Integer, Integer> multW,
                               HashMap<Integer, Integer> divC, HashMap<Integer, Integer> divW) {
        this.multCorrect = multC;
        this.multWrong = multW;
        this.divCorrect = divC;
        this.divWrong = divW;
    }
}
