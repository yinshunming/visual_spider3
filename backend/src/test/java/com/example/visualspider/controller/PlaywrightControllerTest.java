package com.example.visualspider.controller;

import com.example.visualspider.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/postgres",
    "spring.datasource.username=postgres",
    "spring.datasource.password=123456",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "playwright.max-sessions=5",
    "playwright.session-timeout-ms=60000",
    "playwright.headless=true",
    "playwright.browser-args=--no-sandbox"
})
class PlaywrightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testSessionId;

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() {
        if (testSessionId != null) {
            try {
                mockMvc.perform(delete("/api/playwright/sessions/" + testSessionId));
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    void createSession_validUrl_returns201Created() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUrl("https://example.com");

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        SessionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), SessionResponse.class);
        assertThat(response.getSessionId()).isNotNull();
        assertThat(response.getUrl()).isEqualTo("https://example.com");

        testSessionId = response.getSessionId();
    }

    @Test
    void closeSession_validSessionId_returns204() throws Exception {
        testSessionId = createTestSession();

        mockMvc.perform(delete("/api/playwright/sessions/" + testSessionId))
                .andExpect(status().isNoContent());

        testSessionId = null;
    }

    @Test
    void closeSession_invalidSessionId_returns404() throws Exception {
        mockMvc.perform(delete("/api/playwright/sessions/invalid-session-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
    }

    @Test
    void pingSession_validSessionId_returns200() throws Exception {
        testSessionId = createTestSession();

        mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/ping"))
                .andExpect(status().isOk());
    }

    @Test
    void pingSession_invalidSessionId_returns404() throws Exception {
        mockMvc.perform(post("/api/playwright/sessions/invalid-session-id/ping"))
                .andExpect(status().isNotFound());
    }

    @Test
    void navigate_validSessionAndUrl_returns200() throws Exception {
        testSessionId = createTestSession();

        NavigateRequest request = new NavigateRequest();
        request.setUrl("https://example.org");

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/navigate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        NavigateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), NavigateResponse.class);
        assertThat(response.getUrl()).isEqualTo("https://example.org");
    }

    @Test
    void screenshot_validSessionId_returns200() throws Exception {
        testSessionId = createTestSession();

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/screenshot")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andReturn();

        ScreenshotResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ScreenshotResponse.class);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void element_validSessionIdAndCoordinates_returns200() throws Exception {
        testSessionId = createTestSession();

        ElementInfoRequest request = new ElementInfoRequest();
        request.setX(400);
        request.setY(300);

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/element")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ElementInfoResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ElementInfoResponse.class);
        assertThat(response.getTagName()).isNotNull();
    }

    @Test
    void testSelector_validCssSelector_returns200() throws Exception {
        testSessionId = createTestSession();

        TestSelectorRequest request = new TestSelectorRequest();
        request.setSelector("h1");
        request.setType("CSS");

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/test-selector")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        TestSelectorResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), TestSelectorResponse.class);
        assertThat(response.getCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testSelector_invalidSelector_returns400() throws Exception {
        testSessionId = createTestSession();

        TestSelectorRequest request = new TestSelectorRequest();
        request.setSelector("invalid[[[");
        request.setType("CSS");

        mockMvc.perform(post("/api/playwright/sessions/" + testSessionId + "/test-selector")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_SELECTOR"));
    }

    @Test
    void sessionNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/playwright/sessions/nonexistent/element")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":100,\"y\":100}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("SESSION_NOT_FOUND"));
    }

    private String createTestSession() throws Exception {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setUrl("https://example.com");

        MvcResult result = mockMvc.perform(post("/api/playwright/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        SessionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), SessionResponse.class);
        return response.getSessionId();
    }
}