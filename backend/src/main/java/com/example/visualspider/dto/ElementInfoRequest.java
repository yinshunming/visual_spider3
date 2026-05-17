package com.example.visualspider.dto;

import jakarta.validation.constraints.NotNull;

public class ElementInfoRequest {

    @NotNull(message = "x coordinate is required")
    private Integer x;

    @NotNull(message = "y coordinate is required")
    private Integer y;

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}