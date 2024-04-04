package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Builder
@Document("friend_requests")
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {
    private String id;
    private String fromUserId;
    private String toUserId;
    private boolean status;
    private LocalDateTime createdAt;
}
