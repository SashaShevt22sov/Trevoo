package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;
// ======== Нету прав доступа
public class NoPermissionException extends BaseException {
    public NoPermissionException(String message) {
        super("NO_PERMISSION", message, HttpStatus.FORBIDDEN);
    }
}
