package iuh.cnm.bezola.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank(message = "Phone is required")
    @JsonProperty("phone")
    private String phone;
    @NotBlank(message = "Password is required")
    private String password;
    @JsonProperty("role_id")
    private String roleId;
}
