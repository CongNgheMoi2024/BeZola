package iuh.cnm.bezola.models;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Data
@Document("messages")
public class Message {
    @Id
    private String id;
    private String chatId;
    private String senderId;
    private String recipientId;
    private String replyTo;
    @JsonSerialize(using = DateSerializer.class)
    private Date timestamp;
    private String content;
    private String fileName;
    private MessageType type;
    private Status status;
    private List<String> deletedUsers = new ArrayList<>();
    private List<String> attachments;
    private List<String> readBy = new ArrayList<>();
    private User user;
}
