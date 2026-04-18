package Zvonok.infrastructure.storage.dto;

import Zvonok.infrastructure.storage.entity.AccessRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAccessDocumentRequestDto {

    private AccessRule rule;
    private List<Long> userIds;
}
