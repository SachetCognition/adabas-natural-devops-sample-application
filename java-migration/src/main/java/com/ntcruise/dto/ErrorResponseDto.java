package com.ntcruise.dto;

import java.time.LocalDateTime;

/**
 * Structured error response DTO.
 * Maps Natural error constructs (*ERROR-NR, *PROGRAM, *ERROR-LINE)
 * to a JSON error response (NCINMAPP.NSP lines 60-69).
 */
public class ErrorResponseDto {

    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
