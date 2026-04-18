package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;
// ==== Уже друзья
public class AlreadyFriendsException extends BaseException {
    public AlreadyFriendsException(String message) {
        super("ALREADY_FRIENDS", message, HttpStatus.CONFLICT);
    }
}

