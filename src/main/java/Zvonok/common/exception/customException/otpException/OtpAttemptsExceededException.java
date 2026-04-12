package Zvonok.common.exception.customException.otpException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class OtpAttemptsExceededException extends BaseException {

    private static final String CODE = "OTP_ATTEMPTS_EXCEEDED";

    public OtpAttemptsExceededException() {
        super(
                CODE,
                "Превышено количество попыток ввода кода",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
