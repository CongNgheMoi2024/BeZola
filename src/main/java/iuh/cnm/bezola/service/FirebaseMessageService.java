package iuh.cnm.bezola.service;

import com.google.firebase.FirebaseException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import iuh.cnm.bezola.dto.NotificationMessage;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessageService {
    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Autowired
    private UserService userService;

    public String sendNotificationByToken(NotificationMessage notificationMessage) {
        Notification notification = Notification.builder()
                .setTitle(notificationMessage.getTitle())
                .setBody(notificationMessage.getBody())
                .setImage(notificationMessage.getImage())
                .build();

        System.out.println(notificationMessage.getRecipientToken());
        Message message = Message.builder()
                .setNotification(notification)
                .putAllData(notificationMessage.getData())
                .setToken(notificationMessage.getRecipientToken())
                .build();

        try {
            firebaseMessaging.send(message);
            return "Notification has been sent successfully!";
        } catch (FirebaseException e) {
            e.printStackTrace();
            return "Notification has been failed to send!";
        }
    }

    public User saveToken(String jwt, String token) {
        try {
            return userService.saveToken(jwt, token);
        } catch (UserException e) {
            e.printStackTrace();
            return null;
        }
    }
}
