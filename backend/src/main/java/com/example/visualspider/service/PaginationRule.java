package com.example.visualspider.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationRule {

    private static final Logger log = LoggerFactory.getLogger(PaginationRule.class);

    public enum PaginationType {
        INFINITE_SCROLL,
        PAGE_NUMBER,
        NEXT_BUTTON
    }

    private PaginationType type;
    private String nextPageSelector;
    private String pagePattern;
    private Integer startPage;
    private Integer endPage;
    private Integer maxPages;

    public PaginationRule() {
    }

    public PaginationType getType() {
        return type;
    }

    public void setType(PaginationType type) {
        this.type = type;
    }

    public String getNextPageSelector() {
        return nextPageSelector;
    }

    public void setNextPageSelector(String nextPageSelector) {
        this.nextPageSelector = nextPageSelector;
    }

    public String getPagePattern() {
        return pagePattern;
    }

    public void setPagePattern(String pagePattern) {
        this.pagePattern = pagePattern;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(Integer maxPages) {
        this.maxPages = maxPages;
    }

    public String generatePageUrl(String baseUrl, int pageNum) {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("generatePageUrl called with null or blank baseUrl");
            return baseUrl;
        }

        int effectiveStartPage = (startPage != null) ? startPage : 1;

        if (type == PaginationType.PAGE_NUMBER && pagePattern != null && !pagePattern.isBlank()) {
            String result = pagePattern.replace("{page}", String.valueOf(pageNum));
            result = result.replace("{pageNum}", String.valueOf(pageNum));
            result = result.replace("{offset}", String.valueOf((pageNum - effectiveStartPage) * getPageSize()));

            Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher matcher = pattern.matcher(pagePattern);
            if (matcher.find()) {
                log.debug("Page pattern matched: {} for page {}", pagePattern, pageNum);
            }

            if (result.contains("{page}") || result.contains("{pageNum}")) {
                result = result.replace(String.valueOf(pageNum), String.valueOf(pageNum));
            }

            return result;
        }

        if (baseUrl.contains("?")) {
            return baseUrl + "&page=" + pageNum;
        } else {
            return baseUrl + "?page=" + pageNum;
        }
    }

    public boolean shouldStop(int currentPage) {
        int effectiveMaxPages = (maxPages != null) ? maxPages : 50;

        if (currentPage >= effectiveMaxPages) {
            log.debug("Stopping at page {}: reached maxPages {}", currentPage, effectiveMaxPages);
            return true;
        }

        if (endPage != null && currentPage > endPage) {
            log.debug("Stopping at page {}: reached endPage {}", currentPage, endPage);
            return true;
        }

        return false;
    }

    private int getPageSize() {
        return 20;
    }
}
