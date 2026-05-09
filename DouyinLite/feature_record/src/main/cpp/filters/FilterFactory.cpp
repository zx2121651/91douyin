#include "FilterFactory.h"
#include "BrightnessFilter.h"
#include <sstream>
#include <vector>

// Helper to split string
std::vector<std::string> SplitString(const std::string& str, char delimiter) {
    std::vector<std::string> tokens;
    std::string token;
    std::istringstream tokenStream(str);
    while (std::getline(tokenStream, token, delimiter)) {
        if (!token.empty()) {
            tokens.push_back(token);
        }
    }
    return tokens;
}

std::shared_ptr<FilterGroup> FilterFactory::ParseRuleString(std::shared_ptr<rhi::RHIDevice> device, const std::string& ruleString) {
    auto group = std::make_shared<FilterGroup>(device);

    // Naive parsing: split by '@'
    std::vector<std::string> rules = SplitString(ruleString, '@');

    for (const auto& rule : rules) {
        std::vector<std::string> tokens = SplitString(rule, ' ');
        if (tokens.empty()) continue;

        if (tokens[0] == "adjust") {
            if (tokens.size() >= 3 && tokens[1] == "brightness") {
                float value = std::stof(tokens[2]);
                auto filter = std::make_shared<BrightnessFilter>(device);
                filter->SetBrightness(value);
                group->AddFilter(filter);
            }
            // Add more adjusters here (contrast, saturation, etc.)
        }
        // Add more command parsers (curve, blend, etc.)
    }

    return group;
}
