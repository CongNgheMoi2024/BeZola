package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.models.Room;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.responses.RoomWithUserDetailsResponse;
import iuh.cnm.bezola.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
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
}
