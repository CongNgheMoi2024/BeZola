package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/{phone}")
    public ResponseEntity<ApiResponse<?>> getUserByPhone(@PathVariable String phone) {
        try {
            User user = userService.getUserByPhone(phone);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(user)
                            .message("Get user success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .data(userService.getAllUsers())
                        .message("Get all users success")
                        .status(200)
                        .success(true)
                        .build()
        );
    }

    @PostMapping("/users/{id}/add-friend/{friendId}")
    public ResponseEntity<ApiResponse<?>> addFriend(@PathVariable String id, @PathVariable String friendId) {
        try {
            userService.addFriend(id, friendId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("Add friend success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (DataNotFoundException | DataAlreadyExistsException e) {
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
