package Zvonok.common.exception.customException.friendException;

public class TooManyPendingRequestsException extends RuntimeException {
    public TooManyPendingRequestsException(String message) {
        super(message);
    }
}
