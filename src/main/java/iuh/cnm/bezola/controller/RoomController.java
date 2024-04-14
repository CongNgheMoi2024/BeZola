package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.CreateGroupRequest;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.responses.RoomWithUserDetailsResponse;
import iuh.cnm.bezola.service.RoomService;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private final UserService userService;

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
    @PutMapping("/remove-sub-admin/{roomId}/{userId}")
    public ResponseEntity<ApiResponse<?>> removeSubAdmin(@RequestHeader("Authorization") String token, @PathVariable String roomId, @PathVariable String userId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            Room room = roomService.removeSubAdmin(roomId, userId,user);
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
    @DeleteMapping("/delete-room/{roomId}")
    public ResponseEntity<ApiResponse<?>> deleteRoom(@RequestHeader("Authorization") String token, @PathVariable String roomId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        try {
            roomService.deleteRoom(roomId, user);
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
}
