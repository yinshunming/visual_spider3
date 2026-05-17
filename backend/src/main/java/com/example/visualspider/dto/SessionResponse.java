package com.example.visualspider.dto;

public class SessionResponse {

    private String sessionId;
    private String url;
    private Viewport viewport;

    public static class Viewport {
        private int width;
        private int height;

        public Viewport() {}

        public Viewport(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public SessionResponse() {}

    public SessionResponse(String sessionId, String url, int viewportWidth, int viewportHeight) {
        this.sessionId = sessionId;
        this.url = url;
        this.viewport = new Viewport(viewportWidth, viewportHeight);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}