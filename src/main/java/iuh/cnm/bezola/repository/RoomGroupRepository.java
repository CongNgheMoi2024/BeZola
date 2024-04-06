package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.RoomGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomGroupRepository extends MongoRepository<RoomGroup, String> {
}
