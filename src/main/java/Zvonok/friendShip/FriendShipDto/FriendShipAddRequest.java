package Zvonok.friendShip.FriendShipDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendShipAddRequest {

    @NotBlank(message = "Username не может быть пустым")
    @Size(min = 3, max = 50, message = "Username должен быть от 3 до 50 символов")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9._]+$",
            message = "Username может содержать только буквы, цифры, точки и подчеркивания и должен содержать хотя бы одну букву"
    )
    private String username;
}
