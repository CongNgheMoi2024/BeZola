package iuh.cnm.bezola.responses;

import iuh.cnm.bezola.models.Message;
import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.models.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoomWithUserDetailsResponse extends Room {
    private User userRecipient;
    private Message lastMessage;
}
