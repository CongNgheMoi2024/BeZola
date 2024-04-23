package iuh.cnm.bezola.service;

import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import iuh.cnm.bezola.responses.RoomMemberResponse;
import iuh.cnm.bezola.responses.RoomWithUserDetailsResponse;
import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
                .chatId(adminId)
                .senderId(null)
                .isGroup(true)
                .groupName(groupName)
                .members(members)
                .adminId(adminId)
                .build();
        roomRepository.save(room);
        return room.getId();
    }
    public Room addUserToGroup(List<String> members, String roomId) {
        System.out.println("members: "+members);
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        if (room.getMembers().containsAll(members)) {
            throw new RuntimeException("User already in group");
        }

        room.getMembers().addAll(members);
        return roomRepository.save(room);
    }
    public Room renameGroup(String roomId,String groupName) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        room.setGroupName(groupName);
        return roomRepository.save(room);
    }
    public List<User> getSubAdmins(String roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        return userRepository.findAllById(room.getSubAdmins());
    }

    public Room removeUserFromGroup(String roomId, String userId, User reqUser) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = room.getAdminId().equals(reqUser.getId());
        boolean isSubAdmin = room.getSubAdmins().contains(reqUser.getId());
        if(room.getMembers().size()<4){
            throw new RuntimeException("Group must have at least 3 members");
        }
        if (!isAdmin && !isSubAdmin) {
            throw new RuntimeException("You are not admin or sub-admin of this group");
        }
        if (room.getAdminId().equals(userId)) {
            throw new RuntimeException("Admin can't be removed from group");
        }
        if(isSubAdmin && room.getSubAdmins().contains(userId)){
            throw new RuntimeException("You can't removed another sub-admin");
        }
        room.getSubAdmins().remove(user.getId());
        room.getMembers().remove(user.getId());
        return roomRepository.save(room);
    }
    public Room changeAdmin(String roomId, String userId, User reqUser) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = room.getAdminId().equals(reqUser.getId());
        if (!isAdmin) {
            throw new RuntimeException("You are not admin of this group");
        }
        if (room.getAdminId().equals(userId)) {
            throw new RuntimeException("User is already admin");
        }
        room.setAdminId(user.getId());
        room.getSubAdmins().remove(user.getId());
        return roomRepository.save(room);
    }
    public Room leaveGroup(String roomId, User user) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        if (room.getAdminId().equals(user.getId())) {
            throw new RuntimeException("Admin can't leave group");
        }
        if(room.getMembers().size()<4){
            throw new RuntimeException("Group must have at least 3 members");
        }
        room.getSubAdmins().remove(user.getId());
        room.getMembers().remove(user.getId());
        return roomRepository.save(room);
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

    //getRoomByChatId
    public Optional<String> getRoomById(String id) {
        return roomRepository.findById(id)
                .map(Room::getId);
    }

    public Optional<Room> getRoomByRoomId(String id) {
        return roomRepository.findById(id);
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
        // Convert recipientId to ObjectId
        AggregationOperation convertRecipientIdToObjectId = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                return new Document("$addFields", new Document("recipientIdObjectId", new Document("$toObjectId", "$recipientId")));
            }
        };

        // Convert chatId to ObjectId
        AggregationOperation convertChatIdToObjectId = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                Document projectStage = new Document("$addFields", new Document("chatIdObjectId",
                        new Document("$cond", Arrays.asList(
                                new Document("$eq", Arrays.asList(new Document("$strLenCP", "$chatId"), 24)),
                                new Document("$toObjectId", "$chatId"),
                                "$chatId"  // Use original chatId if it is not 24 characters long
                        ))
                ));
                return projectStage;
            }
        };

        // Initial Match Operation including checking membership in group chats
        Criteria senderOrRecipient = Criteria.where("senderId").is(userId);
        Criteria memberInGroup = Criteria.where("isGroup").is(true).and("members").in(userId);
        MatchOperation matchOperation = Aggregation.match(new Criteria().orOperator(senderOrRecipient, memberInGroup));

        // Lookups and other operations remain unchanged
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("users")
                .localField("recipientIdObjectId") // Note: Adjust based on actual field usage for groups
                .foreignField("_id")
                .as("userRecipient");

        LookupOperation lookupLastMessage = LookupOperation.newLookup()
                .from("messages")
                .localField("chatIdObjectId") // Using ObjectId version of chatId for consistency
                .foreignField("chatId")
                .as("lastMessageDetails");

        AggregationOperation filterLastMessage = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                return new Document("$addFields", new Document("lastMessage",
                        new Document("$arrayElemAt", Arrays.asList("$lastMessageDetails", -1))));
            }
        };

        // Final aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                convertRecipientIdToObjectId,
                convertChatIdToObjectId,
                lookupOperation,
                Aggregation.unwind("userRecipient", true),
                lookupLastMessage,
                filterLastMessage,
                Aggregation.unwind("lastMessage", true),
                Aggregation.sort(Sort.Direction.DESC, "lastMessage.timestamp")
        );

        // Execute the aggregation
        List<RoomWithUserDetailsResponse> results = mongoTemplate.aggregate(aggregation, "rooms", RoomWithUserDetailsResponse.class).getMappedResults();

        return results;
    }

    public Room addSubAdmin(String roomId, String userId, User user) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        if(room.getAdminId().equals(user.getId())){
            room.getSubAdmins().add(userId);
            return roomRepository.save(room);
        }
        throw new RuntimeException("You are not admin of this group");
    }

    public Room removeSubAdmin(String roomId, String userId, User user) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        if(room.getAdminId().equals(user.getId())){
            room.getSubAdmins().remove(userId);
            return roomRepository.save(room);
        }
        throw new RuntimeException("You are not admin of this group");
    }

    public RoomMemberResponse getMembers(String roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        List<User> members = userRepository.findAllById(room.getMembers());
        List<User> subAdmins = userRepository.findAllById(room.getSubAdmins());
        User admin = userRepository.findById(room.getAdminId()).orElseThrow(()->new RuntimeException("Admin not found"));
        return RoomMemberResponse.builder()
                .admin(admin)
                .members(members)
                .subAdmins(subAdmins)
                .build();
    }
}
