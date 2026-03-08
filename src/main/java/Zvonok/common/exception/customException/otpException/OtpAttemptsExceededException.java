package Zvonok.common.exception.customException.otpException;

public class OtpAttemptsExceededException extends RuntimeException {
    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}
