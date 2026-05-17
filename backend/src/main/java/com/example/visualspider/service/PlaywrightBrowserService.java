package com.example.visualspider.service;

import com.example.visualspider.config.PlaywrightProperties;
import com.example.visualspider.dto.*;
import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PlaywrightBrowserService {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightBrowserService.class);

    private static final int DEFAULT_VIEWPORT_WIDTH = 1280;
    private static final int DEFAULT_VIEWPORT_HEIGHT = 720;
    private static final int MAX_ELEMENTS_TO_RETURN = 10;
    private static final int PAGE_LOAD_TIMEOUT_MS = 30000;
    private static final int MAX_TEXT_LENGTH = 200;

    @Autowired
    private PlaywrightProperties properties;

    private Playwright playwright;
    private final Map<String, PlaywrightSession> sessions = new ConcurrentHashMap<>();
    private final AtomicInteger sessionCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        log.info("Playwright initialized");
    }

    @PreDestroy
    public void cleanup() {
        for (Map.Entry<String, PlaywrightSession> entry : sessions.entrySet()) {
            try {
                closeBrowserContext(entry.getValue());
            } catch (Exception e) {
                log.warn("Error closing session {} during cleanup: {}", entry.getKey(), e.getMessage());
            }
        }
        sessions.clear();
        if (playwright != null) {
            playwright.close();
            log.info("Playwright closed");
        }
    }

    @Scheduled(fixedRate = 30000)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long timeout = properties.getSessionTimeoutMs();
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, PlaywrightSession> entry : sessions.entrySet()) {
            if (now - entry.getValue().getLastAccessTime() > timeout) {
                toRemove.add(entry.getKey());
            }
        }

        for (String sessionId : toRemove) {
            PlaywrightSession session = sessions.remove(sessionId);
            if (session != null) {
                closeBrowserContext(session);
                sessionCount.decrementAndGet();
                log.info("Session {} expired and removed", sessionId);
            }
        }
    }

    public synchronized String createSession(String url) {
        if (sessions.size() >= properties.getMaxSessions()) {
            throw new SessionLimitException("Maximum sessions reached");
        }

        String sessionId = UUID.randomUUID().toString();
        Browser browser = null;
        BrowserContext context = null;
        Page page = null;

        try {
            BrowserType browserType = playwright.chromium();
            browser = browserType.launch(new BrowserType.LaunchOptions()
                    .setHeadless(properties.isHeadless())
                    .setArgs(Arrays.asList(properties.getBrowserArgs().split(" "))));

            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setViewportSize(DEFAULT_VIEWPORT_WIDTH, DEFAULT_VIEWPORT_HEIGHT)
            );

            page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setTimeout(PAGE_LOAD_TIMEOUT_MS));

            PlaywrightSession session = new PlaywrightSession(sessionId, browser, context, page);
            sessions.put(sessionId, session);
            sessionCount.incrementAndGet();
            session.updateLastAccessTime();

            log.info("Session {} created for URL: {}", sessionId, url);
            return sessionId;

        } catch (Exception e) {
            log.error("Failed to create session for URL: {}", url, e);
            if (page != null) page.close();
            if (context != null) context.close();
            if (browser != null) browser.close();
            throw new PlaywrightServiceException("Failed to launch browser: " + e.getMessage(), e);
        }
    }

    public void closeSession(String sessionId) {
        PlaywrightSession session = sessions.remove(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        closeBrowserContext(session);
        sessionCount.decrementAndGet();
        log.info("Session {} closed", sessionId);
    }

    public void pingSession(String sessionId) {
        PlaywrightSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        session.updateLastAccessTime();
    }

    public NavigateResponse navigate(String sessionId, String url) {
        PlaywrightSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        try {
            session.getPage().navigate(url, new Page.NavigateOptions().setTimeout(PAGE_LOAD_TIMEOUT_MS));
            session.updateLastAccessTime();

            String title = session.getPage().title();
            return new NavigateResponse(url, title);

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Timeout")) {
                throw new PageLoadTimeoutException("Page load timeout");
            }
            throw new PlaywrightServiceException("Navigation failed: " + e.getMessage(), e);
        }
    }

    public ScreenshotResponse getScreenshot(String sessionId, String selector) {
        PlaywrightSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        try {
            Page page = session.getPage();
            byte[] screenshot;
            int width = DEFAULT_VIEWPORT_WIDTH;
            int height = DEFAULT_VIEWPORT_HEIGHT;

            if (selector != null && !selector.isBlank()) {
                Locator element = page.locator(selector);
                if (element.count() > 0) {
                    screenshot = element.first().screenshot();
                    var box = element.first().boundingBox();
                    if (box != null) {
                        width = (int) box.width;
                        height = (int) box.height;
                    }
                } else {
                    screenshot = page.screenshot();
                }
            } else {
                screenshot = page.screenshot();
            }

            session.updateLastAccessTime();
            String base64 = Base64.getEncoder().encodeToString(screenshot);
            return new ScreenshotResponse(base64, width, height);

        } catch (Exception e) {
            log.error("Failed to take screenshot for session {}", sessionId, e);
            throw new PlaywrightServiceException("Failed to take screenshot: " + e.getMessage(), e);
        }
    }

    public ElementInfoResponse getElementAt(String sessionId, int x, int y) {
        PlaywrightSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        try {
            Page page = session.getPage();
            page.mouse().move(x, y);
            page.waitForTimeout(100);

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) page.evaluate(
                    "(() => { " +
                    "  const el = document.elementFromPoint(" + x + ", " + y + "); " +
                    "  if (!el) return null; " +
                    "  const b = el.getBoundingClientRect(); " +
                    "  return { " +
                    "    tagName: el.tagName, " +
                    "    id: el.id || '', " +
                    "    className: el.className || '', " +
                    "    textContent: el.textContent || '', " +
                    "    boundingBox: { x: b.left, y: b.top, width: b.width, height: b.height } " +
                    "  }; " +
                    "})()");

            if (result == null) {
                throw new PlaywrightServiceException("No element found at position (" + x + ", " + y + ")");
            }

            ElementInfoResponse.BoundingBox boundingBox = null;
            @SuppressWarnings("unchecked")
            Map<String, Object> box = (Map<String, Object>) result.get("boundingBox");
            if (box != null) {
                boundingBox = new ElementInfoResponse.BoundingBox(
                        ((Number) box.get("x")).doubleValue(),
                        ((Number) box.get("y")).doubleValue(),
                        ((Number) box.get("width")).doubleValue(),
                        ((Number) box.get("height")).doubleValue()
                );
            }

            session.updateLastAccessTime();

            ElementInfoResponse response = new ElementInfoResponse(
                    (String) result.get("tagName"),
                    (String) result.get("id"),
                    (String) result.get("className"),
                    truncateText((String) result.get("textContent"), MAX_TEXT_LENGTH)
            );
            response.setBoundingBox(boundingBox);
            return response;

        } catch (SessionNotFoundException | PageLoadTimeoutException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get element at ({}, {}) for session {}", x, y, sessionId, e);
            throw new PlaywrightServiceException("Failed to get element: " + e.getMessage(), e);
        }
    }

    public TestSelectorResponse testSelector(String sessionId, String selector, String type) {
        PlaywrightSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }

        try {
            Page page = session.getPage();
            List<ElementInfoResponse> elements = new ArrayList<>();
            int count;

            if ("XPATH".equalsIgnoreCase(type)) {
                count = (int) page.locator("xpath=" + selector).count();
                for (int i = 0; i < Math.min(count, MAX_ELEMENTS_TO_RETURN); i++) {
                    Locator locator = page.locator("xpath=" + selector).nth(i);
                    elements.add(createElementInfo(page, locator));
                }
            } else {
                count = (int) page.locator(selector).count();
                for (int i = 0; i < Math.min(count, MAX_ELEMENTS_TO_RETURN); i++) {
                    Locator locator = page.locator(selector).nth(i);
                    elements.add(createElementInfo(page, locator));
                }
            }

            session.updateLastAccessTime();
            return new TestSelectorResponse(count == 1, count, elements);

        } catch (Exception e) {
            if (e.getMessage() != null &&
                (e.getMessage().contains("Malformed selector") ||
                 e.getMessage().contains("expected"))) {
                throw new InvalidSelectorException("Invalid selector syntax");
            }
            throw new PlaywrightServiceException("Selector test failed: " + e.getMessage(), e);
        }
    }

    private ElementInfoResponse createElementInfo(Page page, Locator locator) {
        try {
            Object el = locator.evaluate("el => el");
            String tagName = (String) page.evaluate("el => el.tagName", el);
            String id = (String) page.evaluate("el => el.id || ''", el);
            String className = (String) page.evaluate("el => el.className || ''", el);
            String textContent = truncateText((String) page.evaluate("el => el.textContent || ''", el), MAX_TEXT_LENGTH);
            var box = (Map<?, ?>) page.evaluate("el => { var b = el.getBoundingClientRect(); return {x: b.left, y: b.top, width: b.width, height: b.height} }", el);

            ElementInfoResponse response = new ElementInfoResponse(tagName, id, className, textContent);
            if (box != null) {
                response.setBoundingBox(new ElementInfoResponse.BoundingBox(
                        ((Number) box.get("x")).doubleValue(),
                        ((Number) box.get("y")).doubleValue(),
                        ((Number) box.get("width")).doubleValue(),
                        ((Number) box.get("height")).doubleValue()
                ));
            }
            return response;
        } catch (Exception e) {
            return new ElementInfoResponse("UNKNOWN", "", "", "");
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        String trimmed = text.trim().replaceAll("\\s+", " ");
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
    }

    private void closeBrowserContext(PlaywrightSession session) {
        try {
            if (session.getPage() != null) {
                session.getPage().close();
            }
        } catch (Exception e) {
            log.warn("Error closing page: {}", e.getMessage());
        }
        try {
            if (session.getContext() != null) {
                session.getContext().close();
            }
        } catch (Exception e) {
            log.warn("Error closing context: {}", e.getMessage());
        }
        try {
            if (session.getBrowser() != null) {
                session.getBrowser().close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser: {}", e.getMessage());
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public static class PlaywrightSession {
        private final String id;
        private final Browser browser;
        private final BrowserContext context;
        private final Page page;
        private volatile long lastAccessTime;

        public PlaywrightSession(String id, Browser browser, BrowserContext context, Page page) {
            this.id = id;
            this.browser = browser;
            this.context = context;
            this.page = page;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public Browser getBrowser() { return browser; }
        public BrowserContext getContext() { return context; }
        public Page getPage() { return page; }
        public long getLastAccessTime() { return lastAccessTime; }

        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    public static class SessionNotFoundException extends RuntimeException {
        public SessionNotFoundException(String message) {
            super(message);
        }
    }

    public static class SessionLimitException extends RuntimeException {
        public SessionLimitException(String message) {
            super(message);
        }
    }

    public static class PageLoadTimeoutException extends RuntimeException {
        public PageLoadTimeoutException(String message) {
            super(message);
        }
    }

    public static class InvalidSelectorException extends RuntimeException {
        public InvalidSelectorException(String message) {
            super(message);
        }
    }

    public static class PlaywrightServiceException extends RuntimeException {
        public PlaywrightServiceException(String message, Throwable cause) {
            super(message, cause);
        }

        public PlaywrightServiceException(String message) {
            super(message);
        }
    }
}