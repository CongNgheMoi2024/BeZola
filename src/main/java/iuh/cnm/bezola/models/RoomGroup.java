package iuh.cnm.bezola.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("room_groups")
public class RoomGroup {
    @Id
    private String id;
    private String groupName;
    private List<String> members;
    private String adminId;
}
