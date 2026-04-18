package Zvonok.core.common.exception.errorResponse;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponse {

    private String code;
    private String message;
    private Map<String, String> details;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, Map<String, String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}
