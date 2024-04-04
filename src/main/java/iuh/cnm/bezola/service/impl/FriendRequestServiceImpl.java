package iuh.cnm.bezola.service.impl;

import iuh.cnm.bezola.models.FriendRequest;
import iuh.cnm.bezola.repository.FriendRequestRepository;
import iuh.cnm.bezola.responses.FriendRequestResponse;
import iuh.cnm.bezola.service.FriendRequestService;
import iuh.cnm.bezola.service.UserService;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean sendFriendRequest(String fromUserId, String toUserId) {
        try {
            friendRequestRepository.save(FriendRequest.builder()
                    .fromUserId(fromUserId)
                    .toUserId(toUserId)
                    .status(false)
                    .createdAt(LocalDateTime.now())
                    .build());
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean acceptFriendRequest(String fromUserId, String toUserId) {
        try {
            Optional<FriendRequest> friendRequest = friendRequestRepository.findByFromUserIdAndToUserId(fromUserId, toUserId);
            if (friendRequest.isEmpty()) {
                return false;
            }

            userService.addFriend(fromUserId, toUserId);
            userService.addFriend(toUserId, fromUserId);

            friendRequestRepository.delete(friendRequest.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean declineFriendRequest(String fromUserId, String toUserId) {
        try {
            Optional<FriendRequest> friendRequest = friendRequestRepository.findByFromUserIdAndToUserId(fromUserId, toUserId);
            System.out.println(friendRequest.get());
            if (friendRequest.isEmpty()) {
                return false;
            }

            friendRequestRepository.delete(friendRequest.get());
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<List<FriendRequestResponse>> getFriendRequestByFromUser(String fromUserId) {
        try {
            // Bước chuyển đổi toUserId sang ObjectId
            System.out.println("1");
            AggregationOperation convertToUserIdToObjectId = new AggregationOperation() {
                @Override
                public Document toDocument(AggregationOperationContext context) {
                    Document projectStage = new Document("$addFields", new Document("toUserIdObjectId", new Document("$toObjectId", "$toUserId")));
                    return projectStage;
                }
            };
            System.out.println("2");

            LookupOperation lookupOperation = LookupOperation.newLookup()
                    .from("users") // Tên bảng/bộ sưu tập của người dùng
                    .localField("toUserIdObjectId") // Trường trong bảng Room để thực hiện join
                    .foreignField("_id") // Trường tương ứng trong bảng người dùng
                    .as("toUser"); // Tên trường output chứa thông tin người dùng

            // Bước unwind để "flatten" mảng toUser
            AggregationOperation unwind = Aggregation.unwind("toUser");

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("fromUserId").is(fromUserId)),
                    convertToUserIdToObjectId,
                    lookupOperation,
                    unwind // Thêm bước unwind vào pipeline
            );

            System.out.println("3");
            List<FriendRequestResponse> friendRequestResponses = mongoTemplate.aggregate(aggregation, "friend_requests", FriendRequestResponse.class).getMappedResults();
            System.out.println("4");

            System.out.println(friendRequestResponses);
            return Optional.of(friendRequestResponses);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<FriendRequestResponse>> getFriendRequestByToUser(String toUserId) {
        try {
            // Bước chuyển đổi toUserId sang ObjectId
            AggregationOperation convertToUserIdToObjectId = new AggregationOperation() {
                @Override
                public Document toDocument(AggregationOperationContext context) {
                    Document projectStage = new Document("$addFields", new Document("fromUserIdObjectId", new Document("$toObjectId", "$fromUserId")));
                    return projectStage;
                }
            };

            LookupOperation lookupOperation = LookupOperation.newLookup()
                    .from("users") // Tên bảng/bộ sưu tập của người dùng
                    .localField("fromUserIdObjectId") // Trường trong bảng Room để thực hiện join
                    .foreignField("_id") // Trường tương ứng trong bảng người dùng
                    .as("fromUser"); // Tên trường output chứa thông tin người dùng

            // Bước unwind để "flatten" mảng fromUser
            AggregationOperation unwind = Aggregation.unwind("fromUser");

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("toUserId").is(toUserId)),
                    convertToUserIdToObjectId,
                    lookupOperation,
                    unwind // Thêm bước unwind vào pipeline
            );

            List<FriendRequestResponse> friendRequestResponses = mongoTemplate.aggregate(aggregation, "friend_requests", FriendRequestResponse.class).getMappedResults();

            return Optional.of(friendRequestResponses);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
