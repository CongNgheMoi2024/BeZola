package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhoneBook {
    private String name;
    private String phone;
}
