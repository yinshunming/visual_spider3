package com.example.visualspider.controller;

import com.example.visualspider.dto.*;
import com.example.visualspider.service.PlaywrightBrowserService;
import com.example.visualspider.service.PlaywrightBrowserService.InvalidSelectorException;
import com.example.visualspider.service.PlaywrightBrowserService.PageLoadTimeoutException;
import com.example.visualspider.service.PlaywrightBrowserService.PlaywrightServiceException;
import com.example.visualspider.service.PlaywrightBrowserService.SessionLimitException;
import com.example.visualspider.service.PlaywrightBrowserService.SessionNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playwright")
public class PlaywrightController {

    @Autowired
    private PlaywrightBrowserService playwrightService;

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody SessionCreateRequest request) {
        String sessionId = playwrightService.createSession(request.getUrl());

        SessionResponse response = new SessionResponse(
                sessionId,
                request.getUrl(),
                1280,
                720
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        playwrightService.closeSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/{sessionId}/ping")
    public ResponseEntity<Void> pingSession(@PathVariable String sessionId) {
        playwrightService.pingSession(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessions/{sessionId}/navigate")
    public ResponseEntity<NavigateResponse> navigate(
            @PathVariable String sessionId,
            @Valid @RequestBody NavigateRequest request) {
        NavigateResponse response = playwrightService.navigate(sessionId, request.getUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/screenshot")
    public ResponseEntity<ScreenshotResponse> screenshot(
            @PathVariable String sessionId,
            @RequestBody(required = false) ScreenshotRequest request) {
        String selector = request != null ? request.getSelector() : null;
        ScreenshotResponse response = playwrightService.getScreenshot(sessionId, selector);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/element")
    public ResponseEntity<ElementInfoResponse> getElement(
            @PathVariable String sessionId,
            @Valid @RequestBody ElementInfoRequest request) {
        ElementInfoResponse response = playwrightService.getElementAt(sessionId, request.getX(), request.getY());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/test-selector")
    public ResponseEntity<TestSelectorResponse> testSelector(
            @PathVariable String sessionId,
            @Valid @RequestBody TestSelectorRequest request) {
        TestSelectorResponse response = playwrightService.testSelector(
                sessionId,
                request.getSelector(),
                request.getType()
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SESSION_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(SessionLimitException.class)
    public ResponseEntity<ErrorResponse> handleSessionLimit(SessionLimitException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SESSION_LIMIT", e.getMessage()));
    }

    @ExceptionHandler(PageLoadTimeoutException.class)
    public ResponseEntity<ErrorResponse> handlePageLoadTimeout(PageLoadTimeoutException e) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(new ErrorResponse("PAGE_LOAD_TIMEOUT", e.getMessage()));
    }

    @ExceptionHandler(InvalidSelectorException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSelector(InvalidSelectorException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_SELECTOR", e.getMessage()));
    }

    @ExceptionHandler(PlaywrightServiceException.class)
    public ResponseEntity<ErrorResponse> handlePlaywrightException(PlaywrightServiceException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("PLAYWRIGHT_ERROR", e.getMessage()));
    }
}