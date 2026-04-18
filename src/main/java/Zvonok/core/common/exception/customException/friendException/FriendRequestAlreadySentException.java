package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class FriendRequestAlreadySentException extends BaseException {
    public FriendRequestAlreadySentException(String message) {
        super("FRIEND_REQUEST_ALREADY_SENT", message, HttpStatus.CONFLICT);
    }
}
