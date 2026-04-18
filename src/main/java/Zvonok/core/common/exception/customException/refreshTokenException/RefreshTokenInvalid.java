package Zvonok.core.common.exception.customException.refreshTokenException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class RefreshTokenInvalid extends BaseException {

    private static final String CODE = "REFRESH_TOKEN_INVALID";

    public RefreshTokenInvalid() {
        super(
                CODE,
                "Недействительный refresh token",
                HttpStatus.UNAUTHORIZED
        );
    }
}