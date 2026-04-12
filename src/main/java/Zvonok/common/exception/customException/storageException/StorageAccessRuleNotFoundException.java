package Zvonok.common.exception.customException.storageException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class StorageAccessRuleNotFoundException extends BaseException {

    private static final String CODE = "STORAGE_ACCESS_RULE_NOT_FOUND";

    public StorageAccessRuleNotFoundException(String accessRule) {
        super(
                CODE,
                String.format("Право доступа (%s) на файл не найдено", accessRule),
                HttpStatus.NOT_FOUND
        );
    }
}