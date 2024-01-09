package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String message;
    private User user;
}
