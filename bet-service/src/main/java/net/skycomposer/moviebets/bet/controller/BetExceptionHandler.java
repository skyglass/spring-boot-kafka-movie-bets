package net.skycomposer.moviebets.bet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import net.skycomposer.moviebets.bet.exception.*;

@RestControllerAdvice
public class BetExceptionHandler {

    @ExceptionHandler({BetOpenDeniedException.class, BetCloseDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse("ACCESS_DENIED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(BetNotFoundException ex) {
        net.skycomposer.moviebets.bet.exception.ErrorResponse response = new net.skycomposer.moviebets.bet.exception.ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({MarketIsOpenException.class, MarketIsClosedException.class, BetAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse("CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
