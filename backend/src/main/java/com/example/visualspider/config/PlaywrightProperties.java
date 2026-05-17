package com.example.visualspider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "playwright")
public class PlaywrightProperties {

    private int maxSessions = 5;
    private long sessionTimeoutMs = 3 * 60 * 1000;
    private boolean headless = true;
    private String browserArgs = "--no-sandbox";
    private int screenshotQuality = 80;

    public int getMaxSessions() {
        return maxSessions;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public long getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(long sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public String getBrowserArgs() {
        return browserArgs;
    }

    public void setBrowserArgs(String browserArgs) {
        this.browserArgs = browserArgs;
    }

    public int getScreenshotQuality() {
        return screenshotQuality;
    }

    public void setScreenshotQuality(int screenshotQuality) {
        this.screenshotQuality = screenshotQuality;
    }
}