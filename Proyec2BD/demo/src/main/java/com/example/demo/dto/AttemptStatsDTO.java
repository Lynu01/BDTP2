package com.example.demo.dto;

public class AttemptStatsDTO {
    private final int attempts5Min;
    private final int maxAttempts;
    private final int resultCode;

    public AttemptStatsDTO(int attempts5Min, int maxAttempts, int resultCode) {
        this.attempts5Min = attempts5Min;
        this.maxAttempts = maxAttempts;
        this.resultCode = resultCode;
    }
    public int getAttempts5Min() { return attempts5Min; }
    public int getMaxAttempts()  { return maxAttempts; }
    public int getResultCode()   { return resultCode; }
}
