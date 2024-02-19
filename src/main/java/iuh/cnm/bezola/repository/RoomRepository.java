package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {

    Optional<Room> findBySenderIdAndRecipientId(String senderId, String recipientId);
}
