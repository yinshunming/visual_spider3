package com.example.visualspider.dto;

public class NavigateResponse {

    private String url;
    private String title;

    public NavigateResponse() {}

    public NavigateResponse(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}