package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;
// ========= Заявка не найдена
public class FriendRequestNotFoundException extends BaseException {
    public FriendRequestNotFoundException(String message) {
        super("FRIEND_REQUEST_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
