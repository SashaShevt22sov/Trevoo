package Zvonok.common.exception.customException.otpException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidOtpCodeException extends BaseException {

    private static final String CODE = "INVALID_OTP_CODE";

    public InvalidOtpCodeException(int attemptsLeft) {
        super(
                CODE,
                "Неверный код подтверждения" + attemptsLeft,
                HttpStatus.BAD_REQUEST
        );
    }
}
