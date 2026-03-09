package Zvonok.common.exception;

import Zvonok.common.exception.customException.friendException.AlreadyFriendsException;
import Zvonok.common.exception.customException.friendException.CannotAddYourselfAsFriendException;
import Zvonok.common.exception.customException.friendException.FriendRequestAlreadySentException;
import Zvonok.common.exception.customException.friendException.NoPermissionException;
import Zvonok.common.exception.customException.jwtException.JwtGenerationException;
import Zvonok.common.exception.customException.jwtException.JwtSecretException;
import Zvonok.common.exception.customException.otpException.InvalidOtpCodeException;
import Zvonok.common.exception.customException.otpException.OtpAttemptsExceededException;
import Zvonok.common.exception.customException.otpException.TooManyRequestsException;
import Zvonok.common.exception.customException.otpException.VerificationExpiredException;
import Zvonok.common.exception.customException.refreshTokenException.RefreshTokenNotFoundException;
import Zvonok.common.exception.customException.userException.InvalidCredentialsException;
import Zvonok.common.exception.customException.userException.InvalidPasswordException;
import Zvonok.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
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

    //  ==================== USER EXCEPTIONS ====================

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null, null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null, null);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPasswordException(InvalidPasswordException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)

    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null, null);
    }

    // ==================== OTP EXCEPTIONS ====================

    @ExceptionHandler(VerificationExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleVerificationExpiredException(VerificationExpiredException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }

    @ExceptionHandler(InvalidOtpCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOtpCodeException(InvalidOtpCodeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, Object>> handleTooManyRequestsException(TooManyRequestsException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }

    @ExceptionHandler(OtpAttemptsExceededException.class)
    public ResponseEntity<Map<String, Object>> handleOtpAttemptsExceededException(OtpAttemptsExceededException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), null, null);
    }

    // ==================== FRIEND ОШИБКИ ====================

    @ExceptionHandler(AlreadyFriendsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyFriendsException(AlreadyFriendsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null, null);
    }

    @ExceptionHandler(FriendRequestAlreadySentException.class)
    public ResponseEntity<Map<String, Object>> handleFriendRequestAlreadySentException(FriendRequestAlreadySentException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null, null);
    }

    @ExceptionHandler(CannotAddYourselfAsFriendException.class)
    public ResponseEntity<Map<String, Object>> handleCannotAddYourselfAsFriendException(
            CannotAddYourselfAsFriendException ex) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }
    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<Map<String, Object>> handleNoPermissionException(
            NoPermissionException ex) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null, null);
    }

    // ==================== JWT ОШИБКИ ====================

    @ExceptionHandler(JwtGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleJwtGenerationException(JwtGenerationException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null, null);
    }

    @ExceptionHandler(JwtSecretException.class)
    public ResponseEntity<Map<String, Object>> handleJwtSecretException(JwtSecretException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null, null);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        Map<String, String> extra = Map.of("code", "REFRESH_TOKEN_MISSING");
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Refresh token обязателен", null, extra);
    }

    // ==================== ОШИБКИ ВАЛИДАЦИИ ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(field, msg);
        });

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Проверьте введённые данные",
                errors,
                null
        );
    }

    // ==================== ОБЩИЕ ОШИБКИ ====================

   @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {

    return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Произошла ошибка на сервере",null,null);
}

// ==================== УТИЛИТЫ ====================

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors, Map<String, String> extraFields) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("validationErrors", validationErrors);

        if (extraFields != null) {
            body.putAll(extraFields);
        }

        return new ResponseEntity<>(body, status);
    }
}
