package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.models.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoomWithUserDetailsResponse extends Room {
    private User userRecipient;
}
