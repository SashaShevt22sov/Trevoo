package Zvonok.common.exception.customException.storageException;

public class StorageAccessDeniedException extends RuntimeException {

    private static final String message = "Доступ к файлу запрещён";

    public StorageAccessDeniedException() {
        super(message);
    }
}
