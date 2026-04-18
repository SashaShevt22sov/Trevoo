package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

// ========= Заявка отклонена или уже друзья
public class FriendRequestAlreadyProcessedException extends BaseException {
    public FriendRequestAlreadyProcessedException(String message) {
        super("FRIEND_REQUEST_ALREADY_PROCESSED", message, HttpStatus.CONFLICT);
    }
}
