package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.models.ChatNotification;
import iuh.cnm.bezola.models.Message;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload Message message) {
        System.out.println("Message: " + message);
        Message savedMessage = messageService.save(message);
        simpMessagingTemplate.convertAndSendToUser(
                message.getRecipientId(),
                "/queue/messages",
                ChatNotification.builder()
                        .id(savedMessage.getId())
                        .senderId(savedMessage.getSenderId())
                        .recipientId(savedMessage.getRecipientId())
                        .content(savedMessage.getContent())
                        .timestamp(savedMessage.getTimestamp())
                        .build()
        );
    }

    @GetMapping("/api/v1/messages/{senderId}/{recipientId}")
    public ResponseEntity<ApiResponse<List<Message>>> findMessages(
            @PathVariable("senderId") String senderId,
            @PathVariable("recipientId") String recipientId
    ) {
        List<Message> messages = messageService.findMessages(senderId, recipientId);

        return ResponseEntity.ok(
                ApiResponse.<List<Message>>builder()
                        .data(messages)
                        .success(true)
                        .status(200)
                        .build());
    }
}
