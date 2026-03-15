package Zvonok.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentInfoResponseDto {

    private Long owner;
    private List<DocumentInfo> documents;
    private Long count;


    @AllArgsConstructor(staticName = "of")
    @Data
    public static class DocumentInfo {
        private UUID documentId;
        private String documentName;
        private OffsetDateTime uploadAt;
        private String url;
    }
}
