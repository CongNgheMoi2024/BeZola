package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.NotificationMessage;
import iuh.cnm.bezola.dto.TokenFcm;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.FirebaseMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    @Autowired
    private FirebaseMessageService firebaseMessageService;

    @PostMapping
    public String sendNotification(@RequestBody NotificationMessage notificationMessage) {
        return firebaseMessageService.sendNotificationByToken(notificationMessage);
    }

    @PostMapping("/save-token")
    public ResponseEntity<ApiResponse<?>> saveToken(@RequestHeader("Authorization") String jwt, @RequestBody TokenFcm token) {
        try {
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(
                                ApiResponse.builder()
                                        .message("Token is required!")
                                        .build()
                        );
            }
            System.out.println("Token: " + token.getToken());
            firebaseMessageService.saveToken(jwt, token.getToken());

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .message("Token has been saved successfully!")
                            .data(token.getToken())
                            .build()
            );
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest()
                    .body(
                            ApiResponse.builder()
                                    .message("Token is required!")
                                    .build()
                    );

        }
    }
}
