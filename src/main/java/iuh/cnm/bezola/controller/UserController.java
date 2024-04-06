package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.ChangePasswordDTO;
import iuh.cnm.bezola.dto.UpdateUserDTO;
import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.S3Service;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final S3Service s3Service;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getUserProfileHandler(@RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserProfileByJwt(jwt);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(user)
                            .message("Get user profile success")
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
    @GetMapping("/phone-book/friends")
    public ResponseEntity<ApiResponse<?>> getFriendsFromPhoneBook(@RequestHeader("Authorization") String jwt) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.getFriendsFromPhoneBook(jwt))
                            .message("Get friends from phone book success")
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
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<?>> getFriends(@RequestHeader("Authorization") String jwt) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.getFriends(jwt))
                            .message("Get friends success")
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
    @PutMapping("/update/{phone}")
    public ResponseEntity<?> updateUser(@PathVariable String phone,@RequestBody UpdateUserDTO updateUserDTO) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.update(phone,updateUserDTO))
                            .message("Update user success")
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

    @PostMapping("/change-password/{id}")
    public ResponseEntity<?> changePasswordById(@PathVariable String id,@RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.changePasswordById(id,changePasswordDTO))
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

    @GetMapping("/{id}/friends/{name}")
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
    @PostMapping("/upload-avatar/{id}")
    public ResponseEntity<?> uploadAvatar(@PathVariable String id, @RequestParam("avatar") MultipartFile avatar) {
        try {
             if(avatar.getSize() > 10*1024*1024)  // >10MB
                 return ResponseEntity.badRequest().body(
                         ApiResponse.builder()
                                 .error("File size too large")
                                 .status(400)
                                 .success(false)
                                 .build()
                 );
             String contentType = avatar.getContentType();
             if(contentType == null || !contentType.startsWith("image/"))
                 return ResponseEntity.badRequest().body(
                         ApiResponse.builder()
                                 .error("Invalid file type")
                                 .status(400)
                                 .success(false)
                                 .build()
                 );
                String imageUrl = s3Service.uploadFileToS3(avatar);
                userService.updateAvatarUser(id, imageUrl);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Upload avatar success")
                        .status(200)
                        .success(true)
                        .build()
        );
    }
    @PostMapping("/upload-image-cover/{id}")
    public ResponseEntity<?> uploadImageCover(@PathVariable String id, @RequestParam("image-cover") MultipartFile imageCover) {
        try {
            if(imageCover.getSize() > 10*1024*1024)  // >10MB
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("File size too large")
                                .status(400)
                                .success(false)
                                .build()
                );
            String contentType = imageCover.getContentType();
            if(contentType == null || !contentType.startsWith("image/"))
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Invalid file type")
                                .status(400)
                                .success(false)
                                .build()
                );
            String imageUrl = s3Service.uploadFileToS3(imageCover);
            userService.updateImageCoverUser(id, imageUrl);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Upload image cover success")
                        .status(200)
                        .success(true)
                        .build()
        );
    }


    @GetMapping("/{phone}/skip-me/{id}")
    public ResponseEntity<ApiResponse<?>> getSkipMe(@PathVariable String phone, @PathVariable String id) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.getSkipMe(phone, id))
                            .message("Get skip me success")
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
