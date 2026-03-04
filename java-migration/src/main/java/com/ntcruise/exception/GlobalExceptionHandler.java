package com.ntcruise.exception;

import com.ntcruise.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler mapping Natural error constructs to Spring equivalents.
 *
 * Maps NCINMAPP.NSP error handling (lines 60-69):
 *   ON ERROR
 *     BACKOUT TRANSACTION
 *     MOVE *ERROR-NR   TO #IN-ERRNR
 *     MOVE *PROGRAM    TO #IN-ERRPRG
 *     MOVE *ERROR-LINE TO #IN-ERRLINE
 *
 * Also maps REINPUT messages:
 *   'Sorry - No Cruise found for Id' -> 404
 *   'Sorry - Selection not available' -> 400
 *   'Sorry - Function key not allocated' -> 400
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles CruiseNotFoundException -> HTTP 404.
     * Maps: REINPUT FULL 'Sorry - No Cruise found for Id' (NCINMAPP.NSP line 82)
     */
    @ExceptionHandler(CruiseNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCruiseNotFound(CruiseNotFoundException ex) {
        logger.warn("Cruise not found: {}", ex.getMessage());
        ErrorResponseDto error = new ErrorResponseDto("Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles all runtime exceptions -> HTTP 500.
     * Maps: ON ERROR / BACKOUT TRANSACTION (NCINMAPP.NSP lines 60-69)
     * Natural equivalent: COMPRESS 'Error:' #ERR-TXT '(' #IN-ERRNR ',' #IN-ERRPRG ',' #IN-ERRLINE ')'
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto("Internal Server Error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handles generic exceptions -> HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponseDto error = new ErrorResponseDto("Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
