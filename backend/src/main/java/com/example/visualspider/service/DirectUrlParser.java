package com.example.visualspider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DirectUrlParser {

    private static final Logger log = LoggerFactory.getLogger(DirectUrlParser.class);

    public List<String> getUrls(String[] seedUrls) {
        if (seedUrls == null || seedUrls.length == 0) {
            log.warn("No seed URLs provided");
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String url : seedUrls) {
            if (url != null && !url.isBlank()) {
                result.add(url.trim());
            }
        }

        if (result.isEmpty()) {
            log.warn("All provided URLs were null or empty");
            return List.of();
        }

        // Deduplicate using LinkedHashSet to preserve order
        return new ArrayList<>(new LinkedHashSet<>(result));
    }
}
