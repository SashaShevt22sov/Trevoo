package Zvonok.storage.controller;

import Zvonok.storage.dto.DocumentInfoResponseDto;
import Zvonok.storage.dto.UploadFileDtoResponse;
import Zvonok.storage.entity.AccessRule;
import Zvonok.storage.service.StorageService;
import Zvonok.auth.userDetails.MyUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Log4j2
public class StorageDataController {

    private final StorageService storageService;

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
            @RequestParam(value = "filename", required = false) String expectedFilename,
            @AuthenticationPrincipal MyUserDetails currentUser
    ) {
        var response = storageService.listDocument(expectedFilename, currentUser);

        return ResponseEntity.ok(response);
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
