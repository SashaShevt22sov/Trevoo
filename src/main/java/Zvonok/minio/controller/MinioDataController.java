package Zvonok.minio.controller;

import Zvonok.minio.dto.UploadFileDtoResponse;
import Zvonok.minio.entity.AccessRule;
import Zvonok.minio.repository.DocumentRepository;
import Zvonok.minio.service.StorageService;
import Zvonok.userDetails.MyUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.security.Principal;
import java.util.UUID;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Log4j2
public class MinioDataController {

    private final StorageService storageService;

    @PostConstruct
    void postInit() {
        log.info("api storage in used");
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadFileDtoResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("access") AccessRule accessRule,
            @AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails currentUser
    ) {
        var fileName = file.getOriginalFilename();
        log.info("Загрузка файла {} на файловое хранилище", fileName);
        var response = storageService.uploadFile(file, currentUser, accessRule);
        log.info("Файл {} загружен на файловое хранилище", fileName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/preview/{documentId}")
    public ResponseEntity<Resource> previewFile(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal @Parameter(hidden = true, required = true) MyUserDetails currentUser
    ) {
        var preview = storageService.previewDocument(documentId, currentUser);

        // Определяем MIME-тип по оригинальному имени файла
        var contentType = URLConnection.guessContentTypeFromName(preview.originalName());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + preview.originalName() + "\"")
                .body(preview.resource());
    }
//
//    @GetMapping("/list")
//    public ResponseEntity<List<String>> listFiles() {
//        List<String> files = storageService.listFiles();
//        return ResponseEntity.ok(files);
//    }
//
//    @GetMapping("/download/{fileName}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
//        log.info("Получение файла {} c файлового хранилища", fileName);
//        return storageService.downloadFile(fileName);
//    }
//
//
//    @DeleteMapping("/{fileName}")
//    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
//        log.info("Удаление файла {} c файлового хранилища", fileName);
//        String response = storageService.deleteFile(fileName);
//        log.info("Файл {} удалён с файлового хранилища", fileName);
//        return ResponseEntity.ok(response);
//    }

}
