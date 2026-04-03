#ifndef CONFIGMANAGER_H
#define CONFIGMANAGER_H

#include <string>
#include <set>
#include <array>

struct EinmaleinsConfig;

class ConfigManager {
public:
    static void saveConfig(const EinmaleinsConfig& config);
    static bool loadConfig(EinmaleinsConfig& config);
    static std::string getConfigPath();

private:
    static std::string intToString(int value);
    static int stringToInt(const std::string& str);
    static std::string escapeJson(const std::string& input);
};

#endif
