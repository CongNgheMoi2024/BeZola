package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomWithUserDetailsResponse extends Room {
    private User userRecipient;
}
