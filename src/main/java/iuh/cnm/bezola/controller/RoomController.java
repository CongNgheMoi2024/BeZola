package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.CreateGroupRequest;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.Message;
import iuh.cnm.bezola.models.MessageType;
import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.responses.RoomMemberResponse;
import iuh.cnm.bezola.responses.RoomWithUserDetailsResponse;
import iuh.cnm.bezola.service.MessageService;
import iuh.cnm.bezola.service.RoomService;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final UserService userService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;

//    @MessageMapping("/delete/group")
    public void processGroup(@Payload String roomId,MessageType type,String content,String senderId) {
        Message message = new Message();
        message.setChatId(roomId);
        message.setContent(content);
        message.setSenderId(senderId);
        message.setType(type);
        messageService.saveGroup(message);
        simpMessagingTemplate.convertAndSendToUser(
                roomId, "/queue/messages",
                message
        );
    }
    public void processGroupNotSave(@Payload String roomId,MessageType type,String content) {
        Message message = new Message();
        message.setChatId(roomId);
        message.setContent(content);
        message.setType(type);
        simpMessagingTemplate.convertAndSendToUser(
                roomId, "/queue/messages",
                message
        );
    }
    @GetMapping("/rooms/{roomId}/call")
    public ResponseEntity<ApiResponse<?>> callGroup(@PathVariable String roomId,@RequestHeader("Authorization") String token) throws UserException{
        User user = userService.findUserProfileByJwt(token);
        try {
            processGroupNotSave(roomId,MessageType.CALL_VIDEO,user.getName());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("Call group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @DeleteMapping("/delete-room/{roomId}")
    public ResponseEntity<ApiResponse<?>> deleteRoom(@RequestHeader("Authorization") String token, @PathVariable String roomId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.getRoomByRoomId(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
            roomService.deleteRoom(roomId, user);
            processGroupNotSave(roomId,MessageType.DELETE_GROUP,room.getGroupName());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("Delete room success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }

    @PostMapping("/rooms/create-room-group")
    public ResponseEntity<ApiResponse<?>> createRoomGroup(@RequestHeader("Authorization") String token,@RequestBody CreateGroupRequest request) throws UserException {
        if(request.getMembers().size() < 2){
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error("Members must be more than 2")
                            .status(400)
                            .success(false)
                            .build()
            );
        }
        User user = userService.findUserProfileByJwt(token);
        try {
            String chatId = roomService.createRoomGroup(request.getGroupName(),user.getId(),request.getMembers());
            for (String member : request.getMembers()) {
                processGroupNotSave(member,MessageType.CREATE_GROUP,chatId);
            }
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(chatId)
                            .message("Create room group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/rooms/{roomId}/add-members")
    public ResponseEntity<ApiResponse<?>> addUserToGroup(@RequestBody List<String> members, @PathVariable String roomId) {
        try {
            Room room = roomService.addUserToGroup(members, roomId);
//            processGroup(roomId,MessageType.ADD_MEMBER,"You have been added to a group");
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Add user to group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/rename-group/{roomId}")
    public ResponseEntity<ApiResponse<?>> renameGroup(@PathVariable String roomId, @RequestBody String groupName) {
        try {
            Room room = roomService.renameGroup(roomId, groupName);
            processGroup(roomId,MessageType.RENAME_GROUP,groupName,room.getAdminId());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Rename group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/rooms/{roomId}/remove-member/{userId}")
    public ResponseEntity<ApiResponse<?>> removeUserFromGroup(@RequestHeader("Authorization") String token, @PathVariable String roomId, @PathVariable String userId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.removeUserFromGroup(roomId, userId,user);
//            processGroup(roomId,MessageType.REMOVE_MEMBER,"You have been removed from group");
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Remove user from group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/add-sub-admin/{roomId}/{userId}")
    public ResponseEntity<ApiResponse<?>> addSubAdmin(@RequestHeader("Authorization") String token, @PathVariable String roomId, @PathVariable String userId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.addSubAdmin(roomId, userId,user);
            User addedUser = userService.findById(userId);
            processGroup(roomId,MessageType.ADD_SUB_ADMIN,addedUser.getName() + " đã được phân làm phó nhóm",user.getId());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Add sub admin success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @GetMapping("/rooms/{roomId}/sub-admins")
    public ResponseEntity<ApiResponse<?>> getSubAdmins(@PathVariable String roomId) {
        try {
            List<User> subAdmins = roomService.getSubAdmins(roomId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(subAdmins)
                            .message("Get sub admins success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }


    @PutMapping("/remove-sub-admin/{roomId}/{userId}")
    public ResponseEntity<ApiResponse<?>> removeSubAdmin(@RequestHeader("Authorization") String token, @PathVariable String roomId, @PathVariable String userId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.removeSubAdmin(roomId, userId,user);
            User removedUser = userService.findById(userId);
            processGroup(roomId,MessageType.REMOVE_SUB_ADMIN,removedUser.getName() + " đã bị xóa quyền phó nhóm",user.getId());

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Remove sub admin success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }


    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<ApiResponse<?>> getRoomByUserIdWithRecipientInfo(@PathVariable String userId) {
        try {
            List<RoomWithUserDetailsResponse> rooms = roomService.getRoomByUserIdWithRecipientInfo(userId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(rooms)
                            .message("Get room success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    
    @GetMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<?>> getRoomById(@PathVariable String id) {
        try {
            Optional<Room> room = roomService.getRoomByRoomId(id);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Get room success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }

    @MessageMapping("/group/remove-member")
    public void removeMember(@Payload Message message) throws UserException {
        message.setType(MessageType.REMOVE_MEMBER);
        Message savedMessage = messageService.saveGroup(message);
        simpMessagingTemplate.convertAndSendToUser(
                message.getChatId(), "/queue/messages",   // /user/{roomId Group}/queue/messages
                savedMessage
        );
    }

    @MessageMapping("/group/add-member")
    public void addMember(@Payload Message message) throws UserException {
        System.out.println(message);
        message.setType(MessageType.ADD_MEMBER);
        User sender = userService.findById(message.getSenderId());
        User user = userService.findById(message.getContent());
        if(user == null){
            throw new UserException("User not found");
        }
        message.setContent(sender.getName() +  " đã thêm " + user.getName() + " vào nhóm");
        Message savedMessage = messageService.saveGroup(message);
        simpMessagingTemplate.convertAndSendToUser(
                message.getChatId(), "/queue/messages",   // /user/{roomId Group}/queue/messages
                savedMessage
        );
        simpMessagingTemplate.convertAndSendToUser(
                user.getId(), "/queue/messages",   // /user/{roomId Group}/queue/messages
                savedMessage
        );
    }
    @GetMapping("/rooms/members/{roomId}")
    public ResponseEntity<ApiResponse<?>> getMembers(@PathVariable String roomId) {
        try {
            RoomMemberResponse members = roomService.getMembers(roomId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(members)
                            .message("Get members success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<?>> leaveGroup(@RequestHeader("Authorization") String token, @PathVariable String roomId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.leaveGroup(roomId, user);
            processGroup(roomId,MessageType.LEAVE_GROUP,user.getName() + " đã rời nhóm",user.getId());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Leave group success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
    @PutMapping("/rooms/{roomId}/change-admin/{userId}")
    public ResponseEntity<ApiResponse<?>> makeAdmin(@RequestHeader("Authorization") String token, @PathVariable String roomId, @PathVariable String userId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.changeAdmin(roomId, userId,user);
            User addedUser = userService.findById(userId);
            processGroup(roomId,MessageType.CHANGE_ADMIN,addedUser.getName()+ " đã được phân làm trưởng nhóm",user.getId());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(room)
                            .message("Change admin success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }
}
