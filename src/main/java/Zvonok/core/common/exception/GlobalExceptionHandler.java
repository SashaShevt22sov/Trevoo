package Zvonok.core.common.exception;

import Zvonok.core.common.exception.customException.BaseException;
import Zvonok.core.common.exception.errorResponse.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(
                        ex.getCode(),
                        ex.getMessage(),
                        null
                ));
    }

    // ==================== ОШИБКИ ВАЛИДАЦИИ ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "Проверьте введённые данные",
                        errors
                )
        );
    }

    // ==================== ОБЩИЕ ОШИБКИ ====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Произошла ошибка на сервере",
                        null
                )
        );
    }


}
