package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.responses.FriendRequestResponse;
import iuh.cnm.bezola.service.impl.FriendRequestServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/friend-requests")
public class FriendRequestController {
    private final FriendRequestServiceImpl friendRequestService;

    @PostMapping("/send-friend-request/{fromUserId}/{toUserId}")
    public ResponseEntity<ApiResponse<?>> sendFriendRequest(@PathVariable String fromUserId, @PathVariable String toUserId) {
        try {
            if (friendRequestService.sendFriendRequest(fromUserId, toUserId)) {
                // Nếu thành công, trả về phản hồi thành công
                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .message("Send friend request success")
                                .status(200)
                                .success(true)
                                .build()
                );
            } else {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Failed to send friend request")
                                .status(400)
                                .success(false)
                                .build()
                );
            }
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

    @PostMapping("/accept-friend-request/{fromUserId}/{toUserId}")
    public ResponseEntity<ApiResponse<?>> acceptFriendRequest(@PathVariable String fromUserId, @PathVariable String toUserId) {
        try {
            if (friendRequestService.acceptFriendRequest(fromUserId, toUserId)) {
                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .message("Accept friend request success")
                                .status(200)
                                .success(true)
                                .build()
                );
            } else {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Failed to accept friend request")
                                .status(400)
                                .success(false)
                                .build()
                );
            }
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

    @PostMapping("/decline-friend-request/{fromUserId}/{toUserId}")
    public ResponseEntity<ApiResponse<?>> declineFriendRequest(@PathVariable String fromUserId, @PathVariable String toUserId) {
        try {
            if (friendRequestService.declineFriendRequest(fromUserId, toUserId)) {
                return ResponseEntity.ok(
                        ApiResponse.builder()
                                .message("Decline friend request success")
                                .status(200)
                                .success(true)
                                .build()
                );
            } else {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Failed to decline friend request")
                                .status(400)
                                .success(false)
                                .build()
                );
            }
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

    @GetMapping("/{fromUserId}")
    public ResponseEntity<ApiResponse<?>> getFriendRequestByFromUser(@PathVariable String fromUserId) {
        try {
            System.out.println("fromUserId = " + fromUserId);
            Optional<List<FriendRequestResponse>> friendRequest = friendRequestService.getFriendRequestByFromUser(fromUserId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(friendRequest)
                            .message("Get friend request success")
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

    @GetMapping("/invitation/{toUserId}")
    public ResponseEntity<ApiResponse<?>> getFriendRequestByToUser(@PathVariable String toUserId) {
        try {
            Optional<List<FriendRequestResponse>> friendRequest = friendRequestService.getFriendRequestByToUser(toUserId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(friendRequest)
                            .message("Get friend request success")
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
