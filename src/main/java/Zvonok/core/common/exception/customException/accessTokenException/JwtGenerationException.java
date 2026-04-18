package Zvonok.core.common.exception.customException.accessTokenException;

public class JwtGenerationException extends RuntimeException {
    public JwtGenerationException(String message) {
        super(message);
    }
}
