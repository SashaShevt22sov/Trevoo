package Zvonok.server.serverDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateServerRequestDto {
    @NotBlank(message = "Название сервера обязательно")
    @Size(min = 2, max = 50, message = "Название должно быть от 3 до 50 символов")
    private String name;
}
