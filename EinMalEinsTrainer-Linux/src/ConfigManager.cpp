#include "ConfigManager.h"
#include "Trainer.h"
#include <fstream>
#include <sstream>
#include <algorithm>
#include <cstdlib>
#include <cctype>

#ifdef _WIN32
#include <windows.h>
#include <shlobj.h>
#else
#include <unistd.h>
#include <sys/types.h>
#include <pwd.h>
#endif

std::string ConfigManager::getConfigPath() {
#ifdef _WIN32
    char* appdata = std::getenv("APPDATA");
    if (appdata) {
        return std::string(appdata) + "\\EinMalEinsTrainer\\config.json";
    }
    return "config.json";
#else
    const char* home = std::getenv("HOME");
    if (!home) {
        struct passwd* pwd = getpwuid(getuid());
        if (pwd) home = pwd->pw_dir;
    }
    if (home) {
        return std::string(home) + "/.config/einmaleins_config";
    }
    return "einmaleins_config";
#endif
}

void ConfigManager::saveConfig(const EinmaleinsConfig& config) {
    std::string path = getConfigPath();
    
#ifdef _WIN32
    std::string dir = path.substr(0, path.find_last_of("\\/"));
    CreateDirectoryA(dir.c_str(), nullptr);
#else
    std::string dir = path.substr(0, path.find_last_of("/"));
    if (!dir.empty()) {
        std::string cmd = "mkdir -p \"" + dir + "\"";
        std::system(cmd.c_str());
    }
#endif

    std::ofstream file(path);
    if (!file.is_open()) return;

    file << "{\n";
    file << "  \"baseNumbers\": [";

    bool first = true;
    for (int num : config.baseNumbers) {
        if (!first) file << ", ";
        file << num;
        first = false;
    }
    file << "],\n";

    file << "  \"multipliers\": {\n";
    for (int i = 1; i <= 10; i++) {
        file << "    \"" << i << "\": [";
        bool firstMult = true;
        for (int mult : config.multipliers[i]) {
            if (!firstMult) file << ", ";
            file << mult;
            firstMult = false;
        }
        file << "]";
        if (i < 10) file << ",";
        file << "\n";
    }
    file << "  }\n";
    file << "}\n";

    file.close();
}

bool ConfigManager::loadConfig(EinmaleinsConfig& config) {
    for (int i = 1; i <= 10; i++) {
        config.multipliers[i].clear();
    }
    
    std::string path = getConfigPath();
    std::ifstream file(path);
    
    if (!file.is_open()) {
        for (int i = 1; i <= 10; i++) {
            config.multipliers[i].clear();
        }
        return false;
    }

    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string content = buffer.str();
    file.close();

    size_t basePos = content.find("\"baseNumbers\"");
    if (basePos == std::string::npos) {
        for (int i = 1; i <= 10; i++) {
            config.multipliers[i].clear();
        }
        return false;
    }

    size_t baseStart = content.find("[", basePos);
    size_t baseEnd = content.find("]", baseStart);
    if (baseStart == std::string::npos || baseEnd == std::string::npos) {
        for (int i = 1; i <= 10; i++) {
            config.multipliers[i].clear();
        }
        return false;
    }

    std::string baseStr = content.substr(baseStart + 1, baseEnd - baseStart - 1);
    std::stringstream ss(baseStr);
    std::string num;
    config.baseNumbers.clear();
    while (std::getline(ss, num, ',')) {
        num.erase(std::remove_if(num.begin(), num.end(), ::isspace), num.end());
        if (!num.empty()) {
            config.baseNumbers.insert(stringToInt(num));
        }
    }

    size_t multSectionPos = content.find("\"multipliers\"");
    for (int i = 1; i <= 10; i++) {
        config.multipliers[i].clear();
        std::string key = "\"" + intToString(i) + "\"";
        size_t multPos = content.find(key, multSectionPos);
        if (multPos != std::string::npos) {
            size_t multStart = content.find("[", multPos);
            size_t multEnd = content.find("]", multStart);
            if (multStart != std::string::npos && multEnd != std::string::npos) {
                std::string multStr = content.substr(multStart + 1, multEnd - multStart - 1);
                std::stringstream ms(multStr);
                std::string m;
                while (std::getline(ms, m, ',')) {
                    m.erase(std::remove_if(m.begin(), m.end(), ::isspace), m.end());
                    if (!m.empty()) {
                        config.multipliers[i].insert(stringToInt(m));
                    }
                }
            }
        }
    }

    return true;
}

std::string ConfigManager::intToString(int value) {
    std::stringstream ss;
    ss << value;
    return ss.str();
}

int ConfigManager::stringToInt(const std::string& str) {
    std::stringstream ss(str);
    int value;
    ss >> value;
    return value;
}

std::string ConfigManager::escapeJson(const std::string& input) {
    std::string output;
    for (char c : input) {
        switch (c) {
            case '"': output += "\\\""; break;
            case '\\': output += "\\\\"; break;
            case '\n': output += "\\n"; break;
            case '\r': output += "\\r"; break;
            default: output += c;
        }
    }
    return output;
}
