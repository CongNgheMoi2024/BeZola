package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomMemberResponse {
    private User admin;
    private List<User> members;
    private List<User> subAdmins;
}
