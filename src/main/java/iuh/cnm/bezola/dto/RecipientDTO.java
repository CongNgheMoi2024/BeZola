package iuh.cnm.bezola.dto;

import lombok.Data;

@Data
public class RecipientDTO {
    private String id;
    private String username;
    private String avatar;
    private String lastSeen;
    private String lastMessage;
}
