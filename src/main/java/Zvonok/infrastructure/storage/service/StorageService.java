package Zvonok.infrastructure.storage.service;

import Zvonok.core.common.exception.customException.storageException.StorageAccessRuleNotFoundException;
import Zvonok.core.common.exception.customException.storageException.StorageDocumentNotFoundException;
import Zvonok.infrastructure.storage.dto.DocumentInfoResponseDto;
import Zvonok.infrastructure.storage.dto.DocumentPreviewResponseDto;
import Zvonok.infrastructure.storage.dto.UploadFileDtoResponse;
import Zvonok.infrastructure.storage.entity.AccessRule;
import Zvonok.infrastructure.storage.entity.Document;
import Zvonok.infrastructure.storage.repository.DocumentAccessRuleRepository;
import Zvonok.infrastructure.storage.repository.DocumentRepository;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userService.UserService;
import Zvonok.features.auth.myUserDetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static Zvonok.infrastructure.storage.entity.AccessRule.PERSONAL;

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

    public List<Long> getAccessByDocumentUserIds(UUID documentId, MyUserDetails currentUser){
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    var err = new StorageDocumentNotFoundException(documentId.toString());
                    log.warn(err.getMessage());
                    return err;
                });

        validationService.validateUpdateRule(document,currentUser);

        var accessDocumentUserIds = document.getAllowedUsers()
                .stream()
                .map(User::getId)
                .filter(id -> !id.equals(document.getOwner().getId()))
                .collect(Collectors.toList());
        accessDocumentUserIds.add(currentUser.getId());

        return accessDocumentUserIds;
    }

    @Transactional
    public UploadFileDtoResponse uploadFile(MultipartFile file, MyUserDetails currentUser, AccessRule access) {
        var accessRule = documentAccessRuleRepository.findDocumentAccessRuleByAccessibilityRule(access)
                .orElseThrow(() -> {
                    var err = new StorageAccessRuleNotFoundException(access.name());
                    log.warn(err.getMessage());
                    return err;
                });

        var document = Document.builder()
                .documentName(String.join("_", UUID.randomUUID().toString(), file.getOriginalFilename()))
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

        validationService.validateUpdateRule(document, currentUser);

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

    public DocumentPreviewResponseDto previewDocument(UUID documentId, MyUserDetails currentUser) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    var err = new StorageDocumentNotFoundException(documentId.toString());
                    log.warn(err.getMessage());
                    return err;
                });

        validationService.validatePreviewAccessDocument(document, currentUser);

        var resource = previewFile(document.getDocumentName());
        return new DocumentPreviewResponseDto(resource, document.getDocumentName());
    }

    @Transactional
    public void deleteDocument(UUID documentId, MyUserDetails currentUser) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> {
                    var err = new StorageDocumentNotFoundException(documentId.toString());
                    log.warn(err.getMessage());
                    return err;
                });

        validationService.validateDeleteAccessDocumet(document, currentUser);


        documentRepository.delete(document);
        deleteFile(document.getDocumentName());
    }

    public DocumentInfoResponseDto listDocument(String q, MyUserDetails currentUser) {

        q = String.join("%","",q,"");
        if(q == null){
            q = "%";
        }
        var documents = documentRepository.findDocumentsByOwnerIdAndDocumentNameLikeIgnoreCase(currentUser.getUser().getId(), q)
                .stream()
                .map(document -> DocumentInfoResponseDto.DocumentInfo.of(
                        document.getId(),
                        document.getDocumentName().substring(37),
                        document.getCreatedAt(),
                        "Пока не готово"
                ))
                .toList();

        return DocumentInfoResponseDto.builder()
                .owner(currentUser.getUser().getId())
                .documents(documents)
                .count((long) documents.size())
                .build();
    }

    /**
     * Проверяет, существует ли дефолтный бакет. Если нет - создает его.
     */
    private void createBucketIfNotExists() {
        storageService.createBucketIfNotExists(defaultBucket);
    }

    /**
     * Загрузка файла в MinIO в дефолтный бакет.
     *
     * @param file     - файл для загрузки
     * @param fileName - имя, под которым файл будет сохранен
     * @return сообщение об успехе
     */
    private String uploadFile(MultipartFile file, String fileName) {
        return storageService.uploadFile(defaultBucket, file, fileName);
    }

    /**
     * Возвращает список имен всех объектов в дефолтном бакете.
     *
     */
    private List<String> listFiles() {
        return storageService.listFiles(defaultBucket);
    }

    /**
     * Скачивает файл из дефолтного бакета MinIO.
     *
     * @param fileName - имя файла для скачивания
     * @return ResponseEntity с ресурсом файла
     */
    private ResponseEntity<Resource> downloadFile(String fileName) {
        return storageService.downloadFile(defaultBucket, fileName);
    }

    /**
     * Удаляет файл из дефолтного бакета минио
     *
     * @param fileName наименование файла
     * @return отчет об операции
     */
    private String deleteFile(String fileName) {
        return storageService.deleteFile(defaultBucket, fileName);
    }

    /**
     * Возвращает файл для предварительного просмотра (inline).
     * Content-Type определяется автоматически.
     *
     * @param fileName имя файла в MinIO
     * @return ResponseEntity с содержимым файла
     */
    private Resource previewFile(String fileName) {
        return storageService.previewFile(defaultBucket, fileName);
    }

}
