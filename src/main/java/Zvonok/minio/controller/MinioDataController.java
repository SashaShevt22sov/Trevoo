package Zvonok.minio.controller;

import Zvonok.minio.dto.DocumentInfoResponseDto;
import Zvonok.minio.dto.UploadFileDtoResponse;
import Zvonok.minio.entity.AccessRule;
import Zvonok.minio.service.StorageService;
import Zvonok.userDetails.MyUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.util.UUID;

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
    public ResponseEntity<UploadFileDtoResponse> uploadDocument(
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
    public ResponseEntity<Resource> previewDocument(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal @Parameter(hidden = true, required = true) MyUserDetails currentUser
    ) {
        var preview = storageService.previewDocument(documentId, currentUser);

        // Определяем MIME-тип по оригинальному имени файла
        var contentType = URLConnection.guessContentTypeFromName(preview.getOriginalName());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + preview.getOriginalName() + "\"")
                .body(preview.getResource());
    }

    @GetMapping("/list")
    public ResponseEntity<DocumentInfoResponseDto> listDocument(
            @AuthenticationPrincipal MyUserDetails currentUser,
            @RequestParam("filename") String expectedFilename
    ) {
        return ResponseEntity.ok(storageService.listDocument(expectedFilename, currentUser));
    }


    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal MyUserDetails currentUser) {
        log.info("Удаление файла {} c файлового хранилища", documentId);
        storageService.deleteDocument(documentId, currentUser);
        log.info("Файл {} удалён с файлового хранилища", documentId);
        return ResponseEntity.ok("Файл удалён с файлового хранилища");
    }

}
