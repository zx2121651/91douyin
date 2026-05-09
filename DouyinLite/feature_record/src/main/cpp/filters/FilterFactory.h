#ifndef FILTER_FACTORY_H
#define FILTER_FACTORY_H

#include <string>
#include <memory>
#include "FilterGroup.h"
#include "../rhi/RHIDevice.h"

class FilterFactory {
public:
    // Parses a CGE-style rule string and returns a constructed FilterGroup.
    // Example rule: "@adjust brightness 0.2 @adjust contrast 1.5"
    static std::shared_ptr<FilterGroup> ParseRuleString(std::shared_ptr<rhi::RHIDevice> device, const std::string& ruleString);
};

#endif // FILTER_FACTORY_H
