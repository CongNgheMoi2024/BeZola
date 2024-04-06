package iuh.cnm.bezola.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Data
@Document("group_messages")
public class MessageGroup extends Message {
    private boolean isGroup;
}
