package Zvonok.core.common.exception.customException.refreshTokenException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends BaseException {

    private static final String CODE = "REFRESH_TOKEN_NOT_FOUND";

    public RefreshTokenNotFoundException() {
        super(
                CODE,
                "Refresh token отсутствует",
                HttpStatus.UNAUTHORIZED
        );
    }
}
