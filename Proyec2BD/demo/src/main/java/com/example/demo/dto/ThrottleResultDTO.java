package com.example.demo.dto;

public class ThrottleResultDTO {

    private final boolean isBlocked;
    private final int secondsRemaining;

    public ThrottleResultDTO(boolean isBlocked, int secondsRemaining) {
        this.isBlocked = isBlocked;
        this.secondsRemaining = secondsRemaining;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}