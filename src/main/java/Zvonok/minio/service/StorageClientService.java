package Zvonok.minio.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageClientService {

    /**
     * Возвращает файл для предварительного просмотра (inline).
     * Content-Type определяется автоматически.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   имя файла в MinIO
     * @return ResponseEntity с содержимым файла
     */
    Resource previewFile(String bucketName, String fileName);

    /**
     * Удаляет файл из MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   - имя файла для удаления
     * @return сообщение об успехе
     */
    String deleteFile(String bucketName, String fileName);

    /**
     * Скачивает файл из MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param fileName   - имя файла для скачивания
     * @return ResponseEntity с ресурсом файла
     */
    ResponseEntity<Resource> downloadFile(String bucketName, String fileName); // Убрать ResponseEntity из ответа

    /**
     * Возвращает список имен всех объектов в бакете.
     *
     * @param bucketName - наименование бакета в который загружаем
     */
    List<String> listFiles(String bucketName);

    /**
     * Загрузка файла в MinIO.
     *
     * @param bucketName - наименование бакета в который загружаем
     * @param file       - файл для загрузки
     * @param fileName   - имя, под которым файл будет сохранен
     * @return сообщение об успехе
     */
    String uploadFile(String bucketName, MultipartFile file, String fileName);

    /**
     * Проверяет, существует ли бакет. Если нет - создает его.
     *
     * @param bucketName - наименование проверяемого/создаваемого бакета
     */
    void createBucketIfNotExists(String bucketName);
}
