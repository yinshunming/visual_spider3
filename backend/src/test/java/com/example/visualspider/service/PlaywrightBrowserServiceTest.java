package com.example.visualspider.service;

import com.example.visualspider.config.PlaywrightProperties;
import com.example.visualspider.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "playwright.max-sessions=5",
    "playwright.session-timeout-ms=60000",
    "playwright.headless=true",
    "playwright.browser-args=--no-sandbox"
})
class PlaywrightBrowserServiceTest {

    @Autowired
    private PlaywrightBrowserService playwrightService;

    private String testSessionId;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        if (testSessionId != null) {
            try {
                playwrightService.closeSession(testSessionId);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    void createSession_validUrl_createsSession() {
        testSessionId = playwrightService.createSession("https://example.com");
        assertThat(testSessionId).isNotNull();
        assertThat(testSessionId).isNotEmpty();
    }

    @Test
    void createSession_maxSessionsExceeded_throwsException() {
        String[] sessionIds = new String[5];
        try {
            for (int i = 0; i < 5; i++) {
                sessionIds[i] = playwrightService.createSession("https://example.com");
            }

            assertThatThrownBy(() -> playwrightService.createSession("https://example.com"))
                    .isInstanceOf(PlaywrightBrowserService.SessionLimitException.class)
                    .hasMessage("Maximum sessions reached");
        } finally {
            for (int i = 0; i < 5; i++) {
                if (sessionIds[i] != null) {
                    try {
                        playwrightService.closeSession(sessionIds[i]);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    @Test
    void closeSession_validSessionId_closesSession() {
        testSessionId = playwrightService.createSession("https://example.com");
        assertThatCode(() -> playwrightService.closeSession(testSessionId))
                .doesNotThrowAnyException();
        testSessionId = null;
    }

    @Test
    void closeSession_invalidSessionId_throwsNotFoundException() {
        assertThatThrownBy(() -> playwrightService.closeSession("invalid-session-id"))
                .isInstanceOf(PlaywrightBrowserService.SessionNotFoundException.class)
                .hasMessageContaining("Session not found");
    }

    @Test
    void pingSession_validSessionId_updatesLastAccessTime() {
        testSessionId = playwrightService.createSession("https://example.com");
        assertThatCode(() -> playwrightService.pingSession(testSessionId))
                .doesNotThrowAnyException();
    }

    @Test
    void pingSession_invalidSessionId_throwsNotFoundException() {
        assertThatThrownBy(() -> playwrightService.pingSession("invalid-session-id"))
                .isInstanceOf(PlaywrightBrowserService.SessionNotFoundException.class);
    }

    @Test
    void getScreenshot_validSessionId_returnsScreenshot() {
        testSessionId = playwrightService.createSession("https://example.com");
        ScreenshotResponse response = playwrightService.getScreenshot(testSessionId, null);
        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void getElementAt_validCoordinates_returnsElementInfo() {
        testSessionId = playwrightService.createSession("https://example.com");
        ElementInfoResponse response = playwrightService.getElementAt(testSessionId, 400, 300);
        assertThat(response).isNotNull();
        assertThat(response.getTagName()).isNotNull();
    }

    @Test
    void testSelector_uniqueSelector_returnsUnique() {
        testSessionId = playwrightService.createSession("https://example.com");
        TestSelectorResponse response = playwrightService.testSelector(testSessionId, "h1", "CSS");
        assertThat(response).isNotNull();
        assertThat(response.getCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testSelector_invalidSelector_throwsException() {
        testSessionId = playwrightService.createSession("https://example.com");
        assertThatThrownBy(() -> playwrightService.testSelector(testSessionId, "invalid[[[", "CSS"))
                .isInstanceOf(PlaywrightBrowserService.InvalidSelectorException.class)
                .hasMessage("Invalid selector syntax");
    }

    @Test
    void navigate_validUrl_updatesPage() {
        testSessionId = playwrightService.createSession("https://example.com");
        NavigateResponse response = playwrightService.navigate(testSessionId, "https://example.org");
        assertThat(response).isNotNull();
        assertThat(response.getUrl()).isEqualTo("https://example.org");
    }

    @Test
    void getActiveSessionCount_correctCount() {
        String[] sessionIds = new String[3];
        try {
            for (int i = 0; i < 3; i++) {
                sessionIds[i] = playwrightService.createSession("https://example.com");
            }
            assertThat(playwrightService.getActiveSessionCount()).isEqualTo(3);
        } finally {
            for (int i = 0; i < 3; i++) {
                if (sessionIds[i] != null) {
                    try {
                        playwrightService.closeSession(sessionIds[i]);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }
}