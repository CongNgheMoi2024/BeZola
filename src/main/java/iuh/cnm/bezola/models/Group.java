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
@Document("groups")
public class Group {
    @Id
    private String id;
    private String groupName;
    private List<String> members;
    private List<String> admins;
}
