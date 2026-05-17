package com.example.visualspider.dto;

import jakarta.validation.constraints.NotBlank;

public class NavigateRequest {

    @NotBlank(message = "URL is required")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}