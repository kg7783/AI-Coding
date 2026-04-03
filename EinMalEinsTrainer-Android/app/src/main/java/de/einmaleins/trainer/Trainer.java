package de.einmaleins.trainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Trainer {
    private static final int MIN_MULTIPLIER = 1;
    private static final int MAX_MULTIPLIER = 10;
    private static final int DEFAULT_VALUE = 1;

    private final Random random;
    private EinmaleinsConfig config;

    private int multFactor1;
    private int multFactor2;
    private int divProduct;
    private int divDivisor;

    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int totalAttempts = 0;

    private HashMap<Integer, Integer> multCorrect;
    private HashMap<Integer, Integer> multWrong;
    private HashMap<Integer, Integer> divCorrect;
    private HashMap<Integer, Integer> divWrong;

    public Trainer() {
        random = new Random();
        config = new EinmaleinsConfig();
        
        multFactor1 = DEFAULT_VALUE;
        multFactor2 = DEFAULT_VALUE;
        divProduct = DEFAULT_VALUE;
        divDivisor = DEFAULT_VALUE;
        
        multCorrect = new HashMap<>();
        multWrong = new HashMap<>();
        divCorrect = new HashMap<>();
        divWrong = new HashMap<>();
        for (int i = MIN_MULTIPLIER; i <= MAX_MULTIPLIER; i++) {
            multCorrect.put(i, 0);
            multWrong.put(i, 0);
            divCorrect.put(i, 0);
            divWrong.put(i, 0);
        }
        
        for (int i = MIN_MULTIPLIER; i <= MAX_MULTIPLIER; i++) {
            config.baseNumbers.add(i);
            for (int j = MIN_MULTIPLIER; j <= MAX_MULTIPLIER; j++) {
                config.multipliers[i].add(j);
            }
        }
        
        generateNewProblems();
    }

    public void setConfig(EinmaleinsConfig config) {
        this.config = config;
    }

    public EinmaleinsConfig getConfig() {
        return config;
    }

    private Set<Integer> getValidMultipliers() {
        Set<Integer> validMultipliers = new HashSet<>();
        for (int i = MIN_MULTIPLIER; i <= MAX_MULTIPLIER; i++) {
            if (!config.multipliers[i].isEmpty()) {
                validMultipliers.add(i);
            }
        }
        return validMultipliers;
    }

    private int selectRandomFromSet(Set<Integer> set) {
        if (set == null || set.isEmpty()) {
            return DEFAULT_VALUE;
        }
        int size = set.size();
        int index = random.nextInt(size);
        int i = 0;
        for (int num : set) {
            if (i == index) {
                return num;
            }
            i++;
        }
        return DEFAULT_VALUE;
    }

    public void generateNewProblems() {
        Set<Integer> validMultipliers = getValidMultipliers();

        if (validMultipliers.isEmpty() || config.baseNumbers.isEmpty()) {
            return;
        }

        int mult1 = selectRandomFromSet(validMultipliers);
        int base1 = selectRandomFromSet(config.multipliers[mult1]);
        
        int mult2 = selectRandomFromSet(validMultipliers);
        int base2 = selectRandomFromSet(config.multipliers[mult2]);
        
        multFactor1 = base1;
        multFactor2 = mult1;
        divProduct = base2 * mult2;
        divDivisor = mult2;
    }

    public MathProblem generateMultiplicationProblem() {
        Set<Integer> validMultipliers = getValidMultipliers();

        if (validMultipliers.isEmpty() || config.baseNumbers.isEmpty()) {
            return null;
        }

        int mult = selectRandomFromSet(validMultipliers);
        int base = selectRandomFromSet(config.multipliers[mult]);
        
        multFactor1 = base;
        multFactor2 = mult;
        
        return new MathProblem(multFactor1, multFactor2, multFactor1 * multFactor2, '*');
    }

    public MathProblem generateDivisionProblem() {
        Set<Integer> validMultipliers = getValidMultipliers();

        if (validMultipliers.isEmpty() || config.baseNumbers.isEmpty()) {
            return null;
        }

        int mult = selectRandomFromSet(validMultipliers);
        int base = selectRandomFromSet(config.multipliers[mult]);
        
        divProduct = base * mult;
        divDivisor = mult;
        
        return new MathProblem(divProduct, divDivisor, divProduct / divDivisor, '/');
    }

    public MathProblem getMultiplicationProblem() {
        if (multFactor1 == 0) multFactor1 = DEFAULT_VALUE;
        if (multFactor2 == 0) multFactor2 = DEFAULT_VALUE;
        return new MathProblem(multFactor1, multFactor2, multFactor1 * multFactor2, '*');
    }

    public MathProblem getDivisionProblem() {
        if (divDivisor == 0) {
            divDivisor = DEFAULT_VALUE;
            divProduct = divDivisor;
        }
        return new MathProblem(divProduct, divDivisor, divProduct / divDivisor, '/');
    }

    public boolean hasValidMultiplicationProblem() {
        Set<Integer> validMultipliers = getValidMultipliers();
        return !validMultipliers.isEmpty() && !config.baseNumbers.isEmpty();
    }

    public boolean hasValidDivisionProblem() {
        return hasValidMultiplicationProblem();
    }

    public void recordCorrectAnswer() {
        correctAnswers++;
        totalAttempts++;
    }

    public void recordCorrectAnswer(int series, boolean isDivision) {
        recordCorrectAnswer();
        if (isDivision) {
            Integer current = divCorrect.get(series);
            divCorrect.put(series, (current != null ? current : 0) + 1);
        } else {
            Integer current = multCorrect.get(series);
            multCorrect.put(series, (current != null ? current : 0) + 1);
        }
    }

    public void recordWrongAnswer() {
        wrongAnswers++;
        totalAttempts++;
    }

    public void recordWrongAnswer(int series, boolean isDivision) {
        recordWrongAnswer();
        if (isDivision) {
            Integer current = divWrong.get(series);
            divWrong.put(series, (current != null ? current : 0) + 1);
        } else {
            Integer current = multWrong.get(series);
            multWrong.put(series, (current != null ? current : 0) + 1);
        }
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

    public void resetStats() {
        correctAnswers = 0;
        wrongAnswers = 0;
        totalAttempts = 0;
        for (int i = MIN_MULTIPLIER; i <= MAX_MULTIPLIER; i++) {
            multCorrect.put(i, 0);
            multWrong.put(i, 0);
            divCorrect.put(i, 0);
            divWrong.put(i, 0);
        }
    }

    public void setStats(int correct, int wrong, int total) {
        this.correctAnswers = correct;
        this.wrongAnswers = wrong;
        this.totalAttempts = total;
    }

    public void setSeriesStats(HashMap<Integer, Integer> multC, HashMap<Integer, Integer> multW,
                             HashMap<Integer, Integer> divC, HashMap<Integer, Integer> divW) {
        this.multCorrect = multC;
        this.multWrong = multW;
        this.divCorrect = divC;
        this.divWrong = divW;
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
}
