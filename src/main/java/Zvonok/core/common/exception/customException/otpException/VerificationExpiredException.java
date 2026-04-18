package Zvonok.core.common.exception.customException.otpException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class VerificationExpiredException extends BaseException {

    private static final String CODE = "VERIFICATION_EXPIRED";

    public VerificationExpiredException() {
        super(
                CODE,
                "Срок действия кода подтверждения истёк",
                HttpStatus.BAD_REQUEST
        );
    }
}
