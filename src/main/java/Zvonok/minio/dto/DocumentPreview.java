package Zvonok.minio.dto;

import org.springframework.core.io.Resource;

public record DocumentPreview(Resource resource, String originalName) {

}
