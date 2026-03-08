package Zvonok.common.exception.customException.otpException;

public class VerificationExpiredException extends RuntimeException {
    public VerificationExpiredException(String message) {
        super(message);
    }
}
