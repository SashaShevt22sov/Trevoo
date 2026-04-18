package Zvonok.core.common.exception.customException.storageException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class StorageAccessDeniedException extends BaseException {

    private static final String CODE = "STORAGE_ACCESS_DENIED";

    public StorageAccessDeniedException() {
        super(
                CODE,
                "Доступ к файлу запрещён",
                HttpStatus.FORBIDDEN
        );
    }
}
