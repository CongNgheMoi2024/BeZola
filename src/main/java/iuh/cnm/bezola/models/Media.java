package iuh.cnm.bezola.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Data
@Document("medias")
public class Media {
    @Id
    private String id;
    private String contentType;
    private String url;
}
