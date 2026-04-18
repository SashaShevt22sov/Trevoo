package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class TooManyPendingRequestsException extends BaseException {
    public TooManyPendingRequestsException(String message) {
        super("TOO_MANY_PENDING_REQUESTS", message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
