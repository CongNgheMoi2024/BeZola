package iuh.cnm.bezola.responses;

import lombok.*;

@Builder
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String phone;
    private String avatar;
}
