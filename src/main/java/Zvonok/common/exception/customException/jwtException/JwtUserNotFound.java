package Zvonok.common.exception.customException.jwtException;

public class JwtUserNotFound extends RuntimeException {
    public JwtUserNotFound(String message) {
        super(message);
    }
}
