package Zvonok.common.exception;

import Zvonok.common.exception.customException.BaseException;
import Zvonok.common.exception.customException.friendException.*;
import Zvonok.common.exception.customException.jwtException.ExpiredJwtException;
import Zvonok.common.exception.customException.jwtException.JwtGenerationException;
import Zvonok.common.exception.customException.jwtException.JwtSecretException;
import Zvonok.common.exception.customException.jwtException.JwtUserNotFound;
import Zvonok.common.exception.customException.otpException.InvalidOtpCodeException;
import Zvonok.common.exception.customException.otpException.OtpAttemptsExceededException;
import Zvonok.common.exception.customException.otpException.TooManyRequestsException;
import Zvonok.common.exception.customException.otpException.VerificationExpiredException;
import Zvonok.common.exception.customException.privateChat.PrivateChatCreateAndGetException;
import Zvonok.common.exception.customException.refreshTokenException.RefreshTokenInvalid;
import Zvonok.common.exception.customException.refreshTokenException.RefreshTokenNotFoundException;
import Zvonok.common.exception.customException.storageException.StorageAccessRuleNotFoundException;
import Zvonok.common.exception.customException.userException.InvalidCredentialsException;
import Zvonok.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.common.exception.errorResponse.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
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
