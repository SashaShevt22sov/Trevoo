package Zvonok.minio.service;

import Zvonok.common.exception.customException.storageException.StorageAccessRuleNotFoundException;
import Zvonok.common.exception.customException.storageException.StorageDocumentNotFoundException;
import Zvonok.minio.dto.DocumentPreview;
import Zvonok.minio.dto.UploadFileDtoResponse;
import Zvonok.minio.entity.AccessRule;
import Zvonok.minio.entity.Document;
import Zvonok.minio.repository.DocumentAccessRuleRepository;
import Zvonok.minio.repository.DocumentRepository;
import Zvonok.user.userService.UserService;
import Zvonok.userDetails.MyUserDetails;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static Zvonok.minio.entity.AccessRule.PERSONAL;

@Service
@RequiredArgsConstructor
@Log4j2
public class StorageService {
    private final StorageClientService storageService;
    private final StorageValidationService validationService;
    private final DocumentRepository documentRepository;
    private final DocumentAccessRuleRepository documentAccessRuleRepository;
    private final UserService userService;

    @Value("${spring.minio-client.default-bucket-name}")
    private String defaultBucket;

    @Transactional
    public UploadFileDtoResponse uploadFile(MultipartFile file, MyUserDetails currentUser, AccessRule access) {
        var accessRule = documentAccessRuleRepository.findDocumentAccessRuleByAccessibilityRule(access)
                .orElseThrow(() -> {
                    var err = new StorageAccessRuleNotFoundException(access.name());
                    log.warn(err.getMessage());
                    return err;
                });

        var document = Document.builder()
                .documentName(String.join("", UUID.randomUUID().toString(), file.getOriginalFilename()))
                .owner(currentUser.getUser())
                .accessRule(accessRule)
                .build();

        document = documentRepository.save(document);
        uploadFile(file, document.getDocumentName());

        return UploadFileDtoResponse.builder()
                .url("Пока не готово")
                .filename(file.getOriginalFilename())
                .message("Document success upload")
                .build();
    }

    @Transactional
    public void updateDocumentAccessRule(UUID documentId, AccessRule accessRule, List<Long> allowedUserIds, MyUserDetails currentUser) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    var err = new StorageDocumentNotFoundException(documentId.toString());
                    log.warn(err.getMessage());
                    return err;
                });

        validationService.validateUpdateRule(document, currentUser.getUser());

        var newAccessRule = documentAccessRuleRepository.findDocumentAccessRuleByAccessibilityRule(accessRule)
                .orElseThrow(() -> {
                    var err = new StorageAccessRuleNotFoundException(accessRule.name());
                    log.warn(err.getMessage());
                    return err;
                });
        var allowedUser = userService.getUserByIds(allowedUserIds);

        document.setAccessRule(newAccessRule);

        if (newAccessRule.getAccessibilityRule().equals(PERSONAL)) {
            document.setAllowedUsers(allowedUser);
        }

        documentRepository.save(document);
    }

    public DocumentPreview previewDocument(UUID documentId, MyUserDetails currentUser) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    var err = new StorageDocumentNotFoundException(documentId.toString());
                    log.warn(err.getMessage());
                    return err;
                });

        validationService.validatePreviewAccessDocument(document, currentUser);

        var resource = previewFile(document.getDocumentName());
        return new DocumentPreview(resource, document.getDocumentName());
    }

    /**
     * Проверяет, существует ли дефолтный бакет. Если нет - создает его.
     */
    private void createBucketIfNotExists() {
        createBucketIfNotExists(defaultBucket);
    }

    /**
     * Проверяет, существует ли бакет. Если нет - создает его.
     *
     * @param bucketName - наименование проверяемого/создаваемого бакета
     */
    private void createBucketIfNotExists(String bucketName) {
        storageService.createBucketIfNotExists(bucketName);
    }

    /**
     * Загрузка файла в MinIO в дефолтный бакет.
     *
     * @param file     - файл для загрузки
     * @param fileName - имя, под которым файл будет сохранен
     * @return сообщение об успехе
     */
    private String uploadFile(MultipartFile file, String fileName) {
        return uploadFile(defaultBucket, file, fileName);
    }

    private String uploadFile(String bucketName, MultipartFile file, String fileName) {
        return storageService.uploadFile(bucketName,file,fileName);
    }

    /**
     * Возвращает список имен всех объектов в дефолтном бакете.
     *
     */
    private List<String> listFiles() {
        return listFiles(defaultBucket);
    }

    private List<String> listFiles(String bucketName) {
        return storageService.listFiles(bucketName);
    }


    /**
     * Скачивает файл из дефолтного бакета MinIO.
     *
     * @param fileName - имя файла для скачивания
     * @return ResponseEntity с ресурсом файла
     */
    private ResponseEntity<Resource> downloadFile(String fileName) {
        return downloadFile(defaultBucket, fileName);
    }

    /**
     * Скачивает файл из MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   - имя файла для скачивания
     * @return ResponseEntity с ресурсом файла
     */
    private ResponseEntity<Resource> downloadFile(String bucketName, String fileName) {
        return storageService.downloadFile(bucketName,fileName);
    }

    private String deleteFile(String fileName) {
        return deleteFile(defaultBucket, fileName);
    }

    private String deleteFile(String bucketName, String fileName) {
        return storageService.deleteFile(bucketName,fileName);
    }

    /**
     * Возвращает файл для предварительного просмотра (inline).
     * Content-Type определяется автоматически.
     *
     * @param fileName имя файла в MinIO
     * @return ResponseEntity с содержимым файла
     */
    private Resource previewFile(String fileName) {
        return previewFile(defaultBucket, fileName);
    }

    private Resource previewFile(String bucketName, String fileName) {
        return storageService.previewFile(bucketName,fileName);
    }
}
