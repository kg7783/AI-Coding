#include "Trainer.h"
#include "Constants.h"
#include "ConfigManager.h"
#include <string>
#include <algorithm>

Trainer::Trainer() : rng(std::random_device{}()) {
    for (int i = 1; i <= 10; i++) {
        config.multipliers[i].clear();
    }
    
    if (!ConfigManager::loadConfig(config)) {
        for (int i = 1; i <= 10; i++) {
            config.baseNumbers.insert(i);
            for (int j = 1; j <= 10; j++) {
                config.multipliers[i].insert(j);
            }
        }
    }
    clearAnswers();
    generateNewProblems();
}

int Trainer::selectRandomFromSet(const std::set<int>& s) {
    if (s.empty()) return 1;
    std::uniform_int_distribution<int> dist(0, static_cast<int>(s.size()) - 1);
    auto it = s.begin();
    std::advance(it, dist(rng));
    return *it;
}

void Trainer::generateNewProblems() {
    hasValidMultiplication = false;
    hasValidDivision = false;
    
    std::set<int> validMultipliers;
    for (int i = 1; i <= 10; i++) {
        if (!config.multipliers[i].empty()) {
            validMultipliers.insert(i);
        }
    }

    if (validMultipliers.empty() || config.baseNumbers.empty()) {
        clearAnswers();
        return;
    }

    int mult1 = selectRandomFromSet(validMultipliers);
    int base1 = selectRandomFromSet(config.multipliers[mult1]);
    
    int mult2 = selectRandomFromSet(validMultipliers);
    int base2 = selectRandomFromSet(config.multipliers[mult2]);
    
    hasValidMultiplication = true;
    hasValidDivision = true;
    multFactor1 = base1;
    multFactor2 = mult1;
    divProduct = base2 * mult2;
    divDivisor = mult2;
    
    clearAnswers();
}

void Trainer::generateMultiplicationProblem() {
    hasValidMultiplication = false;
    
    std::set<int> validMultipliers;
    for (int i = 1; i <= 10; i++) {
        if (!config.multipliers[i].empty()) {
            validMultipliers.insert(i);
        }
    }

    if (validMultipliers.empty() || config.baseNumbers.empty()) {
        return;
    }

    int mult = selectRandomFromSet(validMultipliers);
    int base = selectRandomFromSet(config.multipliers[mult]);
    
    hasValidMultiplication = true;
    multFactor1 = base;
    multFactor2 = mult;
    multAnswer.clear();
}

void Trainer::generateDivisionProblem() {
    hasValidDivision = false;
    
    std::set<int> validMultipliers;
    for (int i = 1; i <= 10; i++) {
        if (!config.multipliers[i].empty()) {
            validMultipliers.insert(i);
        }
    }

    if (validMultipliers.empty() || config.baseNumbers.empty()) {
        return;
    }

    int mult = selectRandomFromSet(validMultipliers);
    int base = selectRandomFromSet(config.multipliers[mult]);
    
    hasValidDivision = true;
    divProduct = base * mult;
    divDivisor = mult;
    divAnswer.clear();
}

MathProblem Trainer::getMultiplicationProblem() const {
    return {multFactor1, multFactor2, multFactor1 * multFactor2, '*'};
}

MathProblem Trainer::getDivisionProblem() const {
    return {divProduct, divDivisor, divProduct / divDivisor, '/'};
}

void Trainer::setMultiplicationAnswer(const std::string& answer) {
    multAnswer = answer;
}

void Trainer::setDivisionAnswer(const std::string& answer) {
    divAnswer = answer;
}

bool Trainer::isMultiplicationCorrect() const {
    if (multAnswer.empty()) return false;
    try {
        return std::stoi(multAnswer) == getMultiplicationProblem().result;
    } catch (...) {
        return false;
    }
}

bool Trainer::isDivisionCorrect() const {
    if (divAnswer.empty()) return false;
    try {
        return std::stoi(divAnswer) == getDivisionProblem().result;
    } catch (...) {
        return false;
    }
}

bool Trainer::hasValidMultiplicationProblem() const {
    return hasValidMultiplication;
}

bool Trainer::hasValidDivisionProblem() const {
    return hasValidDivision;
}

void Trainer::clearAnswers() {
    multAnswer.clear();
    divAnswer.clear();
}

EinmaleinsConfig& Trainer::getConfig() {
    return config;
}

void Trainer::setBaseNumberSelected(int num, bool selected) {
    if (num < 1 || num > 10) return;
    if (selected) {
        config.baseNumbers.insert(num);
    } else {
        config.baseNumbers.erase(num);
    }
    ConfigManager::saveConfig(config);
}

void Trainer::setMultiplierSelected(int multiplier, int base, bool selected) {
    if (multiplier < 1 || multiplier > 10 || base < 1 || base > 10) return;
    if (selected) {
        config.multipliers[multiplier].insert(base);
    } else {
        config.multipliers[multiplier].erase(base);
        if (config.multipliers[multiplier].empty()) {
            config.baseNumbers.erase(multiplier);
        }
    }
    ConfigManager::saveConfig(config);
}
