package com.sunny.CustomWebView;

import android.net.Uri;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a single AdBlock Plus / uBlock Origin filter rule
 * Supports standard filter syntax:
 * - Domain rules: ||example.com^
 * - Path rules: /ads/banner.js
 * - Exception rules: @@||example.com^
 * - Options: $script,$image,$third-party,$domain=example.com
 * - Regex: /pattern/
 * - Element hiding: ##.ad-class (stored but not processed)
 */
public class FilterRule {
    
    // Rule types
    public enum RuleType {
        BLOCKING,           // Normal blocking rule
        EXCEPTION,          // Exception rule (@@)
        ELEMENT_HIDING,     // Element hiding rule (##)
        COMMENT             // Comment or invalid rule
    }
    
    // Resource types
    public enum ResourceType {
        SCRIPT("script"),
        IMAGE("image"),
        STYLESHEET("stylesheet"),
        OBJECT("object"),
        XMLHTTPREQUEST("xmlhttprequest"),
        SUBDOCUMENT("subdocument"),
        DOCUMENT("document"),
        WEBSOCKET("websocket"),
        MEDIA("media"),
        FONT("font"),
        OTHER("other"),
        ALL("all");
        
        private final String value;
        
        ResourceType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static ResourceType fromString(String str) {
            if (str == null) return ALL;
            String lower = str.toLowerCase();
            for (ResourceType type : values()) {
                if (type.value.equals(lower)) {
                    return type;
                }
            }
            return OTHER;
        }
    }
    
    private final String originalRule;
    private final RuleType ruleType;
    
    // Pattern components
    private String pattern;
    private boolean isRegex;
    private Pattern regexPattern;
    
    // Anchors
    private boolean anchorStart;      // |http://
    private boolean anchorEnd;        // .jpg|
    private boolean domainAnchor;     // ||example.com
    private boolean separator;        // Contains ^
    
    // Options
    private Set<ResourceType> includeTypes;
    private Set<ResourceType> excludeTypes;
    private Set<String> includeDomains;
    private Set<String> excludeDomains;
    private boolean thirdParty;
    private Boolean matchCase;
    private boolean important;
    
    // Compiled pattern for fast matching
    private String lowerPattern;
    private String[] patternTokens;
    
    private FilterRule(String rule) {
        this.originalRule = rule;
        
        // Detect rule type
        if (rule.startsWith("!") || rule.startsWith("[") || rule.trim().isEmpty()) {
            this.ruleType = RuleType.COMMENT;
            return;
        } else if (rule.startsWith("@@")) {
            this.ruleType = RuleType.EXCEPTION;
            rule = rule.substring(2);
        } else if (rule.contains("##") || rule.contains("#@#")) {
            this.ruleType = RuleType.ELEMENT_HIDING;
            return; // Element hiding not implemented in WebView context
        } else {
            this.ruleType = RuleType.BLOCKING;
        }
        
        // Parse options
        int optionsIndex = rule.indexOf("$");
        if (optionsIndex > 0) {
            String optionsStr = rule.substring(optionsIndex + 1);
            rule = rule.substring(0, optionsIndex);
            parseOptions(optionsStr);
        }
        
        // Parse pattern
        parsePattern(rule);
        
        // Pre-compute for faster matching
        if (!isRegex && pattern != null) {
            lowerPattern = matchCase != null && matchCase ? pattern : pattern.toLowerCase();
            patternTokens = tokenize(lowerPattern);
        }
    }
    
    /**
     * Create filter rule from string
     */
    public static FilterRule parse(String rule) {
        if (rule == null || rule.trim().isEmpty()) {
            return null;
        }
        try {
            return new FilterRule(rule.trim());
        } catch (Exception e) {
            // Return comment type for invalid rules
            FilterRule invalid = new FilterRule("!" + rule);
            return invalid;
        }
    }
    
    /**
     * Parse the pattern part of the rule
     */
    private void parsePattern(String rule) {
        // Check for regex pattern
        if (rule.startsWith("/") && rule.endsWith("/") && rule.length() > 2) {
            isRegex = true;
            String regexStr = rule.substring(1, rule.length() - 1);
            try {
                regexPattern = Pattern.compile(regexStr);
                pattern = regexStr;
            } catch (Exception e) {
                // Invalid regex, treat as literal
                isRegex = false;
                pattern = rule;
            }
            return;
        }
        
        isRegex = false;
        
        // Check for anchors
        if (rule.startsWith("||")) {
            domainAnchor = true;
            rule = rule.substring(2);
        } else if (rule.startsWith("|")) {
            anchorStart = true;
            rule = rule.substring(1);
        }
        
        if (rule.endsWith("|")) {
            anchorEnd = true;
            rule = rule.substring(0, rule.length() - 1);
        }
        
        // Check for separator
        if (rule.contains("^")) {
            separator = true;
        }
        
        pattern = rule;
    }
    
    /**
     * Parse filter options
     */
    private void parseOptions(String optionsStr) {
        String[] options = optionsStr.split(",");
        
        for (String option : options) {
            option = option.trim();
            if (option.isEmpty()) continue;
            
            boolean inverse = option.startsWith("~");
            if (inverse) {
                option = option.substring(1);
            }
            
            // Resource types
            ResourceType resourceType = ResourceType.fromString(option);
            if (resourceType != ResourceType.ALL && resourceType != ResourceType.OTHER) {
                if (inverse) {
                    if (excludeTypes == null) excludeTypes = new HashSet<>();
                    excludeTypes.add(resourceType);
                } else {
                    if (includeTypes == null) includeTypes = new HashSet<>();
                    includeTypes.add(resourceType);
                }
                continue;
            }
            
            // Domain option
            if (option.startsWith("domain=")) {
                String domainStr = option.substring(7);
                String[] domains = domainStr.split("\\|");
                for (String domain : domains) {
                    domain = domain.trim();
                    if (domain.isEmpty()) continue;
                    
                    if (domain.startsWith("~")) {
                        if (excludeDomains == null) excludeDomains = new HashSet<>();
                        excludeDomains.add(domain.substring(1).toLowerCase());
                    } else {
                        if (includeDomains == null) includeDomains = new HashSet<>();
                        includeDomains.add(domain.toLowerCase());
                    }
                }
                continue;
            }
            
            // Other options
            switch (option.toLowerCase()) {
                case "third-party":
                    thirdParty = !inverse;
                    break;
                case "match-case":
                    matchCase = !inverse;
                    break;
                case "important":
                    important = true;
                    break;
            }
        }
    }
    
    /**
     * Check if this rule matches the given URL and context
     */
    public boolean matches(String url, String domain, ResourceType resourceType, boolean isThirdParty) {
        if (ruleType == RuleType.COMMENT || ruleType == RuleType.ELEMENT_HIDING) {
            return false;
        }
        
        // Check resource type
        if (includeTypes != null && !includeTypes.contains(resourceType)) {
            return false;
        }
        if (excludeTypes != null && excludeTypes.contains(resourceType)) {
            return false;
        }
        
        // Check domain restrictions
        if (domain != null) {
            if (includeDomains != null && !matchesDomain(domain, includeDomains)) {
                return false;
            }
            if (excludeDomains != null && matchesDomain(domain, excludeDomains)) {
                return false;
            }
        }
        
        // Check third-party option
        if (thirdParty && !isThirdParty) {
            return false;
        }
        
        // Check pattern match
        return matchesPattern(url);
    }
    
    /**
     * Check if URL matches the pattern
     */
    private boolean matchesPattern(String url) {
        if (url == null || pattern == null) {
            return false;
        }
        
        // Case sensitivity
        String testUrl = (matchCase != null && matchCase) ? url : url.toLowerCase();
        
        // Regex matching
        if (isRegex && regexPattern != null) {
            return regexPattern.matcher(testUrl).find();
        }
        
        // Domain anchor matching: ||example.com^
        if (domainAnchor) {
            try {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (host == null) return false;
                
                String testHost = (matchCase != null && matchCase) ? host : host.toLowerCase();
                
                // Extract pattern domain and check for separator
                String patternDomain = lowerPattern;
                boolean hasSeparator = patternDomain.endsWith("^");
                int slashIndex = patternDomain.indexOf('/');
                
                // Extract domain part (before slash or separator)
                if (slashIndex > 0) {
                    patternDomain = patternDomain.substring(0, slashIndex);
                }
                
                // Remove separator character for domain matching
                patternDomain = patternDomain.replace("^", "");
                
                // Check if host matches or is subdomain
                if (testHost.equals(patternDomain) || testHost.endsWith("." + patternDomain)) {
                    // If pattern has separator at end of domain (||example.com^)
                    if (hasSeparator && slashIndex < 0) {
                        // Separator means domain should end or be followed by separator char
                        // For URLs like https://ad.doubleclick.net or https://ad.doubleclick.net/path
                        // This should match
                        String path = uri.getPath();
                        String query = uri.getQuery();
                        
                        // Match if:
                        // 1. No path/query (e.g., https://example.com or https://example.com/)
                        // 2. Path starts with / (separator)
                        // 3. Has query parameters (separator ?)
                        if ((path == null || path.isEmpty() || path.equals("/")) && query == null) {
                            return true; // Domain only URL
                        }
                        if (path != null && !path.isEmpty()) {
                            return true; // Has path (/ is a separator)
                        }
                        if (query != null && !query.isEmpty()) {
                            return true; // Has query (? is a separator)
                        }
                        return true; // Default to match for domain anchor with separator
                    }
                    
                    // If pattern has path component, check it too
                    if (slashIndex > 0) {
                        String patternPath = lowerPattern.substring(slashIndex);
                        String testPath = uri.getPath();
                        if (testPath == null) testPath = "";
                        if (!(matchCase != null && matchCase)) {
                            testPath = testPath.toLowerCase();
                        }
                        return matchesSimplePattern(testPath, patternPath);
                    }
                    
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        
        // Anchor start: |http://example.com
        if (anchorStart) {
            if (!testUrl.startsWith(lowerPattern)) {
                return false;
            }
            if (anchorEnd) {
                return testUrl.equals(lowerPattern);
            }
            return true;
        }
        
        // Anchor end: .jpg|
        if (anchorEnd) {
            return testUrl.endsWith(lowerPattern);
        }
        
        // Simple substring match with wildcard and separator support
        return matchesSimplePattern(testUrl, lowerPattern);
    }
    
    /**
     * Match pattern with wildcards (*) and separators (^)
     */
    private boolean matchesSimplePattern(String url, String pattern) {
        // Fast path: exact substring match if no special chars
        if (!pattern.contains("*") && !pattern.contains("^")) {
            return url.contains(pattern);
        }
        
        // Handle wildcards and separators
        String[] parts = pattern.split("\\*", -1);
        int pos = 0;
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            
            if (part.isEmpty() && i == 0) {
                // Pattern starts with *
                continue;
            }
            
            if (part.isEmpty() && i == parts.length - 1) {
                // Pattern ends with *
                return true;
            }
            
            // Handle separator in part
            if (part.contains("^")) {
                part = part.replace("^", "[^\\w\\d_\\-.%]");
                try {
                    Pattern p = Pattern.compile(".*" + part + ".*");
                    if (!p.matcher(url.substring(pos)).find()) {
                        return false;
                    }
                    pos = url.length();
                    continue;
                } catch (Exception e) {
                    part = parts[i].replace("^", "");
                }
            }
            
            int index = url.indexOf(part, pos);
            if (index < 0) {
                return false;
            }
            
            if (i == 0 && !pattern.startsWith("*") && index != 0) {
                return false;
            }
            
            pos = index + part.length();
        }
        
        if (!pattern.endsWith("*") && pos < url.length()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if domain matches any in the set
     */
    private boolean matchesDomain(String domain, Set<String> domainSet) {
        String lowerDomain = domain.toLowerCase();
        
        if (domainSet.contains(lowerDomain)) {
            return true;
        }
        
        // Check parent domains
        int dotIndex = lowerDomain.indexOf('.');
        while (dotIndex > 0 && dotIndex < lowerDomain.length() - 1) {
            lowerDomain = lowerDomain.substring(dotIndex + 1);
            if (domainSet.contains(lowerDomain)) {
                return true;
            }
            dotIndex = lowerDomain.indexOf('.');
        }
        
        return false;
    }
    
    /**
     * Tokenize pattern for fast lookup
     */
    private String[] tokenize(String pattern) {
        if (pattern == null || pattern.length() < 3) {
            return new String[0];
        }
        
        // Extract meaningful tokens (3+ chars, alphanumeric)
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                token.append(c);
            } else {
                if (token.length() >= 3) {
                    tokens.add(token.toString());
                }
                token.setLength(0);
            }
        }
        
        if (token.length() >= 3) {
            tokens.add(token.toString());
        }
        
        return tokens.toArray(new String[0]);
    }
    
    // Getters
    public RuleType getRuleType() {
        return ruleType;
    }
    
    public boolean isException() {
        return ruleType == RuleType.EXCEPTION;
    }
    
    public boolean isImportant() {
        return important;
    }
    
    public String[] getTokens() {
        return patternTokens;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public String getOriginalRule() {
        return originalRule;
    }
    
    @Override
    public String toString() {
        return originalRule;
    }
}
