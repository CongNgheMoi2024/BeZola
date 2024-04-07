package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.models.ChatNotification;
import iuh.cnm.bezola.models.Message;
import iuh.cnm.bezola.models.MessageType;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.MessageService;
import iuh.cnm.bezola.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;
    private final S3Service s3Service;

    @PostMapping(value = "/api/v1/send-file-message",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendFileMessage(@ModelAttribute("files") List<MultipartFile> files,
                           @ModelAttribute("senderId") String senderId,
                           @ModelAttribute("recipientId") String recipientId){
        List<Message> response = new ArrayList<>();
        if(files.isEmpty()){
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status(400)
                    .message("File is required")
                    .build());
        }
        for (MultipartFile file: files) {
            if(file.getSize()> 100*1024*1024){
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status(400)
                        .message("File size must be less than 100MB")
                        .build());
            }
            String fileUrl = s3Service.uploadFileToS3(file);
            String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            Message message = new Message();
            message.setContent(fileUrl);
            message.setRecipientId(recipientId);
            message.setSenderId(senderId);
            message.setTimestamp(new Date());
            if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png")) {
                message.setType(MessageType.IMAGE);
            } else if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".avi") || extension.equalsIgnoreCase(".mov")) {
                message.setType(MessageType.VIDEO);
            } else if (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".wav") || extension.equalsIgnoreCase(".flac")) {
                message.setType(MessageType.AUDIO);
            } else {
                message.setType(MessageType.FILE);
            }
            processMessage(message);
            response.add(message);
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .data(response)
                .status(200)
                .message("File uploaded successfully")
                .build());
    }
    @PostMapping("/api/v1/forward-messages/{messageId}")
    public ResponseEntity<?> forwardMessages(@PathVariable("messageId")String messageId,@RequestBody List<String> recipientIds){
        for ( String recipientId: recipientIds) {
            Message message = messageService.findById(messageId);
            message.setRecipientId(recipientId);
            message.setTimestamp(new Date());
            processMessage(message);
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Message forwarded successfully")
                .build());
    }
    
    @MessageMapping("/chat")  // /app/chat
    public void processMessage(@Payload Message message) {//Payload is messageContent
        System.out.println("Message: " + message);
        Message savedMessage = messageService.save(message);
        simpMessagingTemplate.convertAndSendToUser(
                message.getRecipientId(), "/queue/messages",   // /user/{recipientId}/queue/messages
                ChatNotification.builder()
                        .id(savedMessage.getId())
                        .senderId(savedMessage.getSenderId())
                        .recipientId(savedMessage.getRecipientId())
                        .timestamp(savedMessage.getTimestamp())
                        .content(savedMessage.getContent())
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
