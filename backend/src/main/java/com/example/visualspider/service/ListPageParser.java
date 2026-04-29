package com.example.visualspider.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ListPageParser {

    private static final Logger log = LoggerFactory.getLogger(ListPageParser.class);
    private static final int TIMEOUT_MS = 30000;

    public static class ListPageRule {
        private String containerSelector;
        private String itemUrlSelector;
        private PaginationRule paginationRule;

        public ListPageRule() {
        }

        public String getContainerSelector() {
            return containerSelector;
        }

        public void setContainerSelector(String containerSelector) {
            this.containerSelector = containerSelector;
        }

        public String getItemUrlSelector() {
            return itemUrlSelector;
        }

        public void setItemUrlSelector(String itemUrlSelector) {
            this.itemUrlSelector = itemUrlSelector;
        }

        public PaginationRule getPaginationRule() {
            return paginationRule;
        }

        public void setPaginationRule(PaginationRule paginationRule) {
            this.paginationRule = paginationRule;
        }
    }

    public Document fetchListPage(String url) {
        log.info("Fetching list page: {}", url);
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();
            log.debug("Successfully fetched page: {}, title: {}", url, doc.title());
            return doc;
        } catch (Exception e) {
            log.error("Failed to fetch page: {}, error: {}", url, e.getMessage());
            return null;
        }
    }

    public List<String> extractItemUrls(Document doc, String containerSelector, String itemUrlSelector) {
        List<String> urls = new ArrayList<>();
        if (doc == null) {
            log.warn("Cannot extract item URLs: document is null");
            return urls;
        }

        log.debug("Extracting item URLs with container: {}, item selector: {}", containerSelector, itemUrlSelector);

        Elements containers;
        if (containerSelector != null && !containerSelector.isBlank()) {
            containers = doc.select(containerSelector);
        } else {
            containers = new Elements(doc);
        }

        for (var container : containers) {
            Elements items = container.select(itemUrlSelector);
            for (var item : items) {
                String url = extractUrl(item);
                if (url != null && !url.isBlank()) {
                    urls.add(url);
                }
            }
        }

        log.info("Extracted {} item URLs from page", urls.size());
        return urls;
    }

    private String extractUrl(org.jsoup.nodes.Element item) {
        String href = item.attr("abs:href");
        if (href == null || href.isBlank()) {
            href = item.attr("href");
        }
        if (href == null || href.isBlank()) {
            href = item.attr("src");
        }
        return href;
    }

    public ListPageRule parseListPageRule(String jsonRule) {
        log.info("Parsing list page rule from JSON");
        try {
            ObjectMapper mapper = new ObjectMapper();
            ListPageRule rule = mapper.readValue(jsonRule, ListPageRule.class);
            log.debug("Successfully parsed list page rule");
            return rule;
        } catch (Exception e) {
            log.error("Failed to parse list page rule: {}", e.getMessage());
            return null;
        }
    }

    public List<String> crawlAllPages(String startUrl, ListPageRule rule) {
        log.info("Starting to crawl all pages starting from: {}", startUrl);
        LinkedHashSet<String> allUrls = new LinkedHashSet<>();

        if (rule == null) {
            log.warn("ListPageRule is null, crawling single page only");
            Document doc = fetchListPage(startUrl);
            if (doc != null) {
                allUrls.addAll(extractItemUrls(doc, rule.getContainerSelector(), rule.getItemUrlSelector()));
            }
            return new ArrayList<>(allUrls);
        }

        PaginationRule paginationRule = rule.getPaginationRule();
        String containerSelector = rule.getContainerSelector();
        String itemUrlSelector = rule.getItemUrlSelector();

        if (paginationRule == null) {
            log.info("No pagination rule found, crawling single page");
            Document doc = fetchListPage(startUrl);
            if (doc != null) {
                allUrls.addAll(extractItemUrls(doc, containerSelector, itemUrlSelector));
            }
            return new ArrayList<>(allUrls);
        }

        PaginationRule.PaginationType type = paginationRule.getType();
        log.info("Pagination type: {}", type);

        switch (type) {
            case INFINITE_SCROLL -> {
                log.info("Handling INFINITE_SCROLL pagination - requires JavaScript rendering, returning initial URLs only");
                Document doc = fetchListPage(startUrl);
                if (doc != null) {
                    allUrls.addAll(extractItemUrls(doc, containerSelector, itemUrlSelector));
                }
            }
            case PAGE_NUMBER -> {
                int page = paginationRule.getStartPage() != null ? paginationRule.getStartPage() : 1;
                while (!paginationRule.shouldStop(page)) {
                    String pageUrl = paginationRule.generatePageUrl(startUrl, page);
                    log.info("Crawling page {}: {}", page, pageUrl);
                    Document doc = fetchListPage(pageUrl);
                    if (doc != null) {
                        List<String> pageUrls = extractItemUrls(doc, containerSelector, itemUrlSelector);
                        if (pageUrls.isEmpty()) {
                            log.info("No more URLs found on page {}, stopping", page);
                            break;
                        }
                        allUrls.addAll(pageUrls);
                    }
                    page++;
                }
            }
            case NEXT_BUTTON -> {
                String currentUrl = startUrl;
                int page = 1;
                int maxPages = paginationRule.getMaxPages() != null ? paginationRule.getMaxPages() : 50;

                while (page <= maxPages) {
                    log.info("Crawling page {}: {}", page, currentUrl);
                    Document doc = fetchListPage(currentUrl);
                    if (doc == null) {
                        log.warn("Failed to fetch page, stopping pagination");
                        break;
                    }

                    List<String> pageUrls = extractItemUrls(doc, containerSelector, itemUrlSelector);
                    if (pageUrls.isEmpty()) {
                        log.info("No URLs found on page {}, stopping", page);
                        break;
                    }
                    allUrls.addAll(pageUrls);

                    Elements nextButton = doc.select(paginationRule.getNextPageSelector());
                    if (nextButton.isEmpty()) {
                        log.info("No next button found, stopping pagination");
                        break;
                    }

                    String nextPageHref = nextButton.first().attr("abs:href");
                    if (nextPageHref == null || nextPageHref.isBlank() || nextPageHref.equals(currentUrl)) {
                        log.info("Next button has no valid href, stopping pagination");
                        break;
                    }

                    currentUrl = nextPageHref;
                    page++;
                }
            }
            default -> log.warn("Unknown pagination type: {}", type);
        }

        log.info("Crawling complete. Total unique URLs collected: {}", allUrls.size());
        return new ArrayList<>(allUrls);
    }
}
