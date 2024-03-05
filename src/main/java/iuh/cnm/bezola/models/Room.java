package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("rooms")
public class Room {
    @Id
    private String id;
    private String chatId;
    private String senderId;
    private String recipientId;
    private String lastMessage;
}
