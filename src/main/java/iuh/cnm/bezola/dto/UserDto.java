package iuh.cnm.bezola.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    @NotEmpty(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
//    @NotEmpty(message = "Email is required")
//    @Email(message = "Email is invalid")
//    private String email;
    @NotEmpty(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "Password must be at least 6 characters, including letters, numbers, and special characters")
    private String password;
    @NotEmpty(message = "Retype password is required")
    @JsonProperty("retype_password")
    private String retypePassword;
    @NotEmpty(message = "Phone is required")
    @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
    private String phone;
//    private String avatar;
    private boolean sex;
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;
//    @JsonProperty("online_status")
//    private boolean onlineStatus;
//    @NotNull(message = "Role id is required")
    @JsonProperty("role_id")
    private String roleId;
}
