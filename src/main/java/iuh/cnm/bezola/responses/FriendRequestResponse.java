package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.FriendRequest;
import iuh.cnm.bezola.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestResponse extends FriendRequest {
    private User toUser;
    private User fromUser;
}
