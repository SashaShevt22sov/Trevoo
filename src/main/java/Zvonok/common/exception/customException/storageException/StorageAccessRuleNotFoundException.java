package Zvonok.common.exception.customException.storageException;

public class StorageAccessRuleNotFoundException extends RuntimeException{

    private static final String message = "Право доступа (%s) на файл не найдено";

    public StorageAccessRuleNotFoundException(String accessRule) {
        super(String.format(message,accessRule));
    }
}
