package Zvonok.storage.service;

import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class MinioStorageClientService implements StorageClientService{

    private final MinioClient minioClient;

    @Override
    public Resource previewFile(String bucketName, String fileName) {
        try {
            // Получаем объект из MinIO
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            // Возвращаем файл с заголовком Content-Disposition: inline
            return new InputStreamResource(stream);
        } catch (Exception e) {
            log.error("Ошибка при preview файла '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Не удалось загрузить файл для просмотра", e);
        }
    }

    @Override
    public String deleteFile(String bucketName, String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Файл '{}' успешно удален.", fileName);
            return "Файл успешно удален: " + fileName;
        } catch (Exception e) {
            log.error("Ошибка при удалении файла '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Не удалось удалить файл", e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String bucketName, String fileName) {
        try {
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error("Ошибка при скачивании файла '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Не удалось скачать файл", e);
        }
    }

    @Override
    public List<String> listFiles(String bucketName) {
        var listFilesInBucket = new ArrayList<String>();
        try {
            var results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            for (Result<Item> result : results) {
                var item = result.get();
                listFilesInBucket.add(item.objectName());
            }
            log.info("Получен список файлов. Найдено {} объектов.", listFilesInBucket.size());
        } catch (Exception e) {
            log.error("Ошибка при получении списка файлов: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить список файлов", e);
        }
        return listFilesInBucket;
    }

    @Override
    public String uploadFile(String bucketName, MultipartFile file, String fileName) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Файл '{}' успешно загружен.", fileName);
            return "Файл успешно загружен: " + fileName;
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Не удалось загрузить файл", e);
        }
    }

    @Override
    public void createBucketIfNotExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Бакет '{}' успешно создан.", bucketName);
            } else {
                log.info("Бакет '{}' уже существует.", bucketName);
            }
        } catch (Exception e) {
            log.error("Ошибка при проверке/создании бакета: {}", e.getMessage());
            throw new RuntimeException("Не удалось инициализировать бакет MinIO", e);
        }
    }
}
