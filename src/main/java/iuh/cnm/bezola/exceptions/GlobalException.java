package iuh.cnm.bezola.exceptions;

import iuh.cnm.bezola.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<?>> UserException(UserException ex, WebRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(null)
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> globalException(Exception ex, WebRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(null)
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build()
        );
    }
}
