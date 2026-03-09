package Zvonok.minio.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class MinioService {
    private final MinioClient minioClient;
    @Value("${spring.minio-client.default-bucket-name}")
    private String defaultBucket;

    /**
     * Проверяет, существует ли дефолтный бакет. Если нет - создает его.
     */
    public void createBucketIfNotExists() {
        createBucketIfNotExists(defaultBucket);
    }

    /**
     * Проверяет, существует ли бакет. Если нет - создает его.
     *
     * @param bucketName - наименование проверяемого/создаваемого бакета
     */
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

    /**
     * Загрузка файла в MinIO в дефолтный бакет.
     *
     * @param file     - файл для загрузки
     * @param fileName - имя, под которым файл будет сохранен
     * @return сообщение об успехе
     */
    public String uploadFile(MultipartFile file, String fileName) {
        return uploadFile(defaultBucket, file, fileName);
    }

    /**
     * Загрузка файла в MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param file       - файл для загрузки
     * @param fileName   - имя, под которым файл будет сохранен
     * @return сообщение об успехе
     */
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

    /**
     * Возвращает список имен всех объектов в дефолтном бакете.
     *
     */
    public List<String> listFiles() {
        return listFiles(defaultBucket);
    }

    /**
     * Возвращает список имен всех объектов в бакете.
     *
     * @param bucketName - наименование бакета в который загружаем
     */
    private List<String> listFiles(String bucketName) {
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


    /**
     * Скачивает файл из дефолтного бакета MinIO.
     *
     * @param fileName - имя файла для скачивания
     * @return ResponseEntity с ресурсом файла
     */
    public ResponseEntity<Resource> downloadFile(String fileName) {
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

    /**
     * Удаляет файл из дефолтного бакета MinIO.
     *
     * @param fileName - имя файла для удаления
     * @return сообщение об успехе
     */
    public String deleteFile(String fileName) {
        return deleteFile(defaultBucket, fileName);
    }

    /**
     * Удаляет файл из MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   - имя файла для удаления
     * @return сообщение об успехе
     */
    private String deleteFile(String bucketName, String fileName) {
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

    /**
     * Возвращает файл для предварительного просмотра (inline).
     * Content-Type определяется автоматически.
     *
     * @param fileName имя файла в MinIO
     * @return ResponseEntity с содержимым файла
     */
    public ResponseEntity<Resource> previewFile(String fileName) {
        return previewFile(defaultBucket, fileName);
    }

    /**
     * Возвращает файл для предварительного просмотра (inline).
     * Content-Type определяется автоматически.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   имя файла в MinIO
     * @return ResponseEntity с содержимым файла
     */
    public ResponseEntity<Resource> previewFile(String bucketName, String fileName) {
        try {
            // Получаем объект из MinIO
            var stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            // Определяем MIME-тип по имени файла
            var contentType = URLConnection.guessContentTypeFromName(fileName);
            if (contentType == null) {
                // Если не удалось определить, устанавливаем универсальный бинарный поток
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // Возвращаем файл с заголовком Content-Disposition: inline
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error("Ошибка при preview файла '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Не удалось загрузить файл для просмотра", e);
        }
    }
}
