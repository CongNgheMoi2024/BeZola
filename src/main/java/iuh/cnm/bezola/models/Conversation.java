package iuh.cnm.bezola.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Data
@Document("conversations")
public class Conversation {
    @Id
    private String id;
    private List<String> participantIds;
    private String lastMessageId;
}
