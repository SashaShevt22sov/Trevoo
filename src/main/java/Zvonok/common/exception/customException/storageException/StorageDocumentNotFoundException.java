package Zvonok.common.exception.customException.storageException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class StorageDocumentNotFoundException extends BaseException {

    private static final String CODE = "STORAGE_DOCUMENT_NOT_FOUND";

    public StorageDocumentNotFoundException(String id) {
        super(
                CODE,
                String.format("Файл с идентификатором %s не найден", id),
                HttpStatus.NOT_FOUND
        );
    }
}
