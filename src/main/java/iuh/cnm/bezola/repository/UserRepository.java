package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
