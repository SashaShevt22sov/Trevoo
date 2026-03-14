package Zvonok.common.exception.customException.storageException;

public class StorageDocumentNotFoundException extends RuntimeException {

    private static final String message = "Файл с идентификатором %s не найден";

    public StorageDocumentNotFoundException(String id) {
        super(String.format(message, id));
    }
}
