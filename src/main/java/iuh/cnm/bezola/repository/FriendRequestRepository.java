package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    Optional<FriendRequest> findByFromUserIdAndToUserId(String fromUserId, String toUserId);

    Optional<List<FriendRequest>> findAllByFromUserId(String fromUserId);
}
