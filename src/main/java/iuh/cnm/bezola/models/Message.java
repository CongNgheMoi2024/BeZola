package iuh.cnm.bezola.models;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Data
@Document("messages")
public class Message {
    @Id
    private String id;
    private String conversationId;
    private String senderId;
    private DateTime timestamp;
    private String text;
    private List<String> attachments;
    private List<String> readBy;
}
