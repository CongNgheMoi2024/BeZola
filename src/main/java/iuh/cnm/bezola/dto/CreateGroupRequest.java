package iuh.cnm.bezola.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CreateGroupRequest {
    private String groupName;
    private List<String> members;
}
