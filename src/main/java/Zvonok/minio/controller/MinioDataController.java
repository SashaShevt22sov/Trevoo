package Zvonok.minio.controller;

import Zvonok.minio.service.MinioService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "spring.minio-client", name = "enabled-default-api", havingValue = "true"
)
@Log4j2
public class MinioDataController {

    private final MinioService minioService;

    @PostConstruct
    void postInit() {
        log.info("Default API minio used");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        var fileName = file.getOriginalFilename();
        log.info("Загрузка файла {} на файловое хранилище", fileName);
        var response = minioService.uploadFile(file, fileName);
        log.info("Файл {} загружен на файловое хранилище", fileName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        List<String> files = minioService.listFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        log.info("Получение файла {} c файлового хранилища", fileName);
        return minioService.downloadFile(fileName);
    }

    @GetMapping("/preview/{fileName}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
        return minioService.previewFile(fileName);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        log.info("Удаление файла {} c файлового хранилища", fileName);
        String response = minioService.deleteFile(fileName);
        log.info("Файл {} удалён с файлового хранилища", fileName);
        return ResponseEntity.ok(response);
    }

}
