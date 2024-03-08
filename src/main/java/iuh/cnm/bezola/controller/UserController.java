package iuh.cnm.bezola.controller;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import iuh.cnm.bezola.dto.ChangePasswordDTO;
import iuh.cnm.bezola.dto.UpdateUserDTO;
import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Value("${accessKey}")
    private String accessKey;

    @Value("${secretKey}")
    private String secretKey;

    @Value("${bucketName}")
    private String bucketName;

    private final S3Client s3Client = S3Client.builder()
            .region(Region.AP_SOUTHEAST_1)
            .credentialsProvider(() -> AwsBasicCredentials.create(accessKey, secretKey))
            .build();

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
                String imageUrl = uploadImageToS3(avatar);
                userService.updateAvatarUser(id, imageUrl);
        } catch (DataNotFoundException | IOException e) {
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
            String imageUrl = uploadImageToS3(imageCover);
            userService.updateImageCoverUser(id, imageUrl);
        } catch (DataNotFoundException | IOException e) {
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
    private String uploadImageToS3(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(uniqueFileName)
                            .acl(String.valueOf(CannedAccessControlList.PublicRead))
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
        } catch (S3Exception e) {
            throw new IOException("Failed to upload image to S3");
        }
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(uniqueFileName)).toExternalForm();
    }
}
