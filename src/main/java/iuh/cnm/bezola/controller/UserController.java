package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.ChangePasswordDTO;
import iuh.cnm.bezola.dto.ForgotPasswordDTO;
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
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{phone}")
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
    @PutMapping("/change-password/{phone}")
    public ResponseEntity<?> changePassword(@PathVariable String phone,@RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.changePassword(phone,changePasswordDTO))
                            .message("Update user password success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }

    @GetMapping("")
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

    @PostMapping("/{id}/add-friend/{friendId}")
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

    @GetMapping("/users/{id}/friends/{name}")
    public ResponseEntity<ApiResponse<?>> getFriendByName(@PathVariable String id, @PathVariable String name) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.getFriendByName(id, name))
                            .message("Get friend success")
                            .status(200)
                            .success(true)
                            .build()
            );
        } catch (DataNotFoundException e) {
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
