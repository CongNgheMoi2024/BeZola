package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Data
@Document("users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private boolean onlineStatus;
}