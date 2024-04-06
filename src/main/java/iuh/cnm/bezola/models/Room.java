package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Document("rooms")
public class Room {
    @Id
    private String id;
    private String chatId;
    private String senderId;
    private String recipientId;
    private boolean isGroup;
    private String groupName;
    private List<String> members = new ArrayList<>();
    private String adminId;
    private List<String> subAdmins = new ArrayList<>();
}
