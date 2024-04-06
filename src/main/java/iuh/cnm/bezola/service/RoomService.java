package iuh.cnm.bezola.service;

import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import iuh.cnm.bezola.responses.RoomWithUserDetailsResponse;
import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;

    public String createRoomGroup(String groupName, String adminId, List<String> members){
        members.add(adminId);
        Room room = Room.builder()
                .chatId(adminId+System.currentTimeMillis())
                .senderId(adminId)
                .isGroup(true)
                .groupName(groupName)
                .members(members)
                .adminId(adminId)
                .build();
        roomRepository.save(room);
        return room.getChatId();
    }
    public Room addUserToGroup(String userId, String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        room.getMembers().add(user.getId());
        return roomRepository.save(room);
    }
    public Room renameGroup(String roomId,String groupName) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        room.setGroupName(groupName);
        return roomRepository.save(room);
    }
    public Room removeUserFromGroup(String roomId, String userId, User reqUser) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        if(room.getSubAdmins()!=null){
            if(room.getSubAdmins().contains(reqUser.getId()) && !room.getAdminId().equals(userId)){
                room.getMembers().remove(userId);
                return roomRepository.save(room);
            }
        }
        if(room.getAdminId().equals(reqUser.getId())){
            room.getMembers().remove(user.getId());
            return roomRepository.save(room);
        }else if(room.getMembers().contains(reqUser.getId())) {
            if (reqUser.getId().equals(user.getId())) {
                room.getMembers().remove(user.getId());
                return roomRepository.save(room);
            }
        }
        throw new RuntimeException("You can't remove another user");
    }
    public void deleteRoom(String roomId, User userReq) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        if(room.getAdminId().equals(userReq.getId())){
            roomRepository.delete(room);
            return;
        }
        throw new RuntimeException("You are not admin of this group");
    }

    public Optional<String> getRoomId(String senderId, String recipientId, boolean createNewRoomIfNotExist) {
        return roomRepository.findBySenderIdAndRecipientId(senderId, recipientId)
                .map(Room::getChatId)
                .or(() -> {
                    if (createNewRoomIfNotExist) {
                        var chatId = createChatId(senderId, recipientId);
                        return Optional.of(chatId);
                    } else {
                        return Optional.empty();
                    }
                });
    }

    private String createChatId(String senderId, String recipientId) {
        var chatId = String.format("%s_%s", senderId, recipientId);

        Room senderRecipient = Room.builder()
                .chatId(chatId)
                .senderId(senderId)
                .isGroup(false)
                .recipientId(recipientId)
                .build();

        Room recipientSender = Room.builder()
                .chatId(chatId)
                .isGroup(false)
                .senderId(recipientId)
                .recipientId(senderId)
                .build();

        roomRepository.save(senderRecipient);
        roomRepository.save(recipientSender);

        return chatId;
    }

    public List<RoomWithUserDetailsResponse> getRoomByUserIdWithRecipientInfo(String userId) {
        // Bước chuyển đổi recipientId sang ObjectId
        AggregationOperation convertRecipientIdToObjectId = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                Document projectStage = new Document("$addFields", new Document("recipientIdObjectId", new Document("$toObjectId", "$recipientId")));
                return projectStage;
            }
        };

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("users") // Tên bảng/bộ sưu tập của người dùng
                .localField("recipientIdObjectId") // Trường trong bảng Room để thực hiện join
                .foreignField("_id") // Trường tương ứng trong bảng người dùng
                .as("userRecipient"); // Tên trường output chứa thông tin người dùng

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("senderId").is(userId)),
                convertRecipientIdToObjectId,
                lookupOperation,
                Aggregation.unwind("userRecipient", true) // Giải nén kết quả join, `true` cho phép giữ lại các bản ghi không có kết quả join
        );

        List<RoomWithUserDetailsResponse> results = mongoTemplate.aggregate(aggregation, "rooms", RoomWithUserDetailsResponse.class).getMappedResults();

        System.out.println(results);
        return results;
    }
}
