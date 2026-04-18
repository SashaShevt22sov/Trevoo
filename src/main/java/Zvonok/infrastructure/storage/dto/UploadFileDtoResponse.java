package Zvonok.infrastructure.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadFileDtoResponse {
    private String message;
    private String filename;
    private String url;
}
