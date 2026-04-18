package Zvonok.core.common.exception.customException.otpException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends BaseException {

    private static final String CODE = "OTP_TOO_MANY_REQUESTS";

    public TooManyRequestsException(long waitSeconds) {
        super(
                CODE,
                "Повторная отправка доступна через " + waitSeconds + " сек.",
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
