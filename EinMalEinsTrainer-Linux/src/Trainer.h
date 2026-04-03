#ifndef TRAINER_H
#define TRAINER_H

#include <random>
#include <array>
#include <vector>
#include <set>

struct MathProblem {
    int num1;
    int num2;
    int result;
    char operation;
};

struct EinmaleinsConfig {
    std::set<int> baseNumbers;
    std::array<std::set<int>, 11> multipliers;
};

class Trainer {
public:
    Trainer();

    void generateNewProblems();
    void generateMultiplicationProblem();
    void generateDivisionProblem();
    MathProblem getMultiplicationProblem() const;
    MathProblem getDivisionProblem() const;

    void setMultiplicationAnswer(const std::string& answer);
    void setDivisionAnswer(const std::string& answer);

    bool isMultiplicationCorrect() const;
    bool isDivisionCorrect() const;

    bool wasMultiplicationChecked() const;
    bool wasDivisionChecked() const;
    bool hasValidMultiplicationProblem() const;
    bool hasValidDivisionProblem() const;

    void clearAnswers();

    EinmaleinsConfig& getConfig();
    void setBaseNumberSelected(int num, bool selected);
    void setMultiplierSelected(int baseNum, int multiplier, bool selected);

private:
    std::mt19937 rng;

    int multFactor1;
    int multFactor2;
    int divProduct;
    int divDivisor;

    std::string multAnswer;
    std::string divAnswer;

    bool hasValidMultiplication;
    bool hasValidDivision;

    bool multChecked;
    bool divChecked;

    EinmaleinsConfig config;

    int selectRandomFromSet(const std::set<int>& s);
    std::set<int> getConfiguredMultipliers();
    std::set<int> getValidBasesForMultiplier(int multiplier);
};

#endif
