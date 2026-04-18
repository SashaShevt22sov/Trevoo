package Zvonok.core.common.exception.customException.accessTokenException;

public class ExpiredJwtException extends RuntimeException {
    public ExpiredJwtException(String message) {
        super(message);
    }
}
