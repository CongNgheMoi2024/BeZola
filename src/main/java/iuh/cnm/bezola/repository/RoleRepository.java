package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String>{

}
