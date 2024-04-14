package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.*;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.service.MessageService;
import iuh.cnm.bezola.service.S3Service;
import iuh.cnm.bezola.service.UserService;
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
    private final UserService userService;

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
            String fileName = file.getOriginalFilename();
            String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            Message message = new Message();
            message.setContent(fileUrl);
            message.setStatus(Status.SENT);
            message.setRecipientId(recipientId);
            message.setSenderId(senderId);
            message.setFileName(fileName);
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



    @DeleteMapping("/api/v1/recall-messages/{messageId}")
    public ResponseEntity<?> recallMessage(@PathVariable("messageId") String messageId){
        messageService.recallMessage(messageId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Message deleted successfully")
                .build());
    }
    @PutMapping("/api/v1/delete-messages/{messageId}")
    public ResponseEntity<?> deleteMessages(@RequestHeader("Authorization") String token,@PathVariable("messageId") String messageId) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        messageService.deleteMessage(user.getId(),messageId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Message deleted successfully")
                .build());
    }
    @PostMapping("/api/v1/forward-messages/{messageId}")
    public ResponseEntity<?> forwardMessages(@RequestHeader("Authorization") String token,@PathVariable("messageId")String messageId,@RequestBody List<String> recipientIds) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        Message message = messageService.findById(messageId);
        for ( String recipientId: recipientIds) {
            Message newMessage = new Message();
            newMessage.setSenderId(user.getId());
            newMessage.setStatus(Status.SENT);
            newMessage.setContent(message.getContent());
            newMessage.setFileName(message.getFileName());
            newMessage.setType(message.getType());
            newMessage.setAttachments(message.getAttachments());
            newMessage.setChatId(message.getChatId());
            newMessage.setReadBy(new ArrayList<>());
            newMessage.setRecipientId(recipientId);
            newMessage.setTimestamp(new Date());
            processMessage(newMessage);
        }
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Message forwarded successfully")
                .build());
    }

    @PostMapping("/api/v1/forward-messages-group/{messageId}")
    public ResponseEntity<?> forwardMessagesGroup(@RequestHeader("Authorization") String token,@PathVariable("messageId")String messageId,@RequestBody List<String> roomIds) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        Message message = messageService.findById(messageId);
        for ( String roomId: roomIds) {
            Message newMessage = new Message();
            newMessage.setSenderId(user.getId());
            newMessage.setStatus(Status.SENT);
            newMessage.setContent(message.getContent());
            newMessage.setFileName(message.getFileName());
            newMessage.setType(message.getType());
            newMessage.setAttachments(message.getAttachments());
            newMessage.setChatId(roomId);
            newMessage.setReadBy(new ArrayList<>());
            newMessage.setTimestamp(new Date());
            processMessageGroup(newMessage);
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
                savedMessage
        );
    }

    @MessageMapping("/chat/group")  // /app/chat
    public void processMessageGroup(@Payload Message message) {//Payload is messageContent
        System.out.println("Message: " + message);
        Message savedMessage = messageService.saveGroup(message);
        simpMessagingTemplate.convertAndSendToUser(
                message.getChatId(), "/queue/messages",   // /user/{recipientId}/queue/messages
                savedMessage
        );
    }

    @PostMapping("/api/v1/reply-message/{messageId}")
    public ResponseEntity<?> replyMessage(@PathVariable("messageId") String messageId,@RequestBody Message message) throws UserException {
        message.setReplyTo(messageId);
        processMessage(message);
        return ResponseEntity.ok(message);
    }

    @MessageMapping("/delete") // /app/delete
    public void deleteMessage(@Payload String messageId) {
        Message message = messageService.findById(messageId);
        messageService.recallMessage(messageId);
        message.setType(MessageType.RECALL);
        simpMessagingTemplate.convertAndSendToUser(
                message.getRecipientId(), "/queue/messages",   // /user/{recipientId}/queue/messages
                message
        );
    }

    @MessageMapping("/delete/group") // /app/delete/group
    public void deleteMessageGroup(@Payload String messageId) {
        Message message = messageService.findById(messageId);
        messageService.recallMessage(messageId);
        message.setType(MessageType.RECALL);
        simpMessagingTemplate.convertAndSendToUser(
                message.getChatId(), "/queue/messages",   // /user/{recipientId}/queue/messages
                message
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
    @GetMapping("/api/v1/image-video-messages/{senderId}/{recipientId}")
    public ResponseEntity<ApiResponse<List<Message>>> findImageVideoMessages(
            @PathVariable("senderId") String senderId,
            @PathVariable("recipientId") String recipientId
    ) {
        List<Message> messages = messageService.findImageVideoMessages(senderId, recipientId);

        return ResponseEntity.ok(
                ApiResponse.<List<Message>>builder()
                        .data(messages)
                        .success(true)
                        .status(200)
                        .build());
    }
    @GetMapping("/api/v1/file-messages/{senderId}/{recipientId}")
    public ResponseEntity<ApiResponse<List<Message>>> findFileMessages(
            @PathVariable("senderId") String senderId,
            @PathVariable("recipientId") String recipientId
    ) {
        List<Message> messages = messageService.findFileMessages(senderId, recipientId);

        return ResponseEntity.ok(
                ApiResponse.<List<Message>>builder()
                        .data(messages)
                        .success(true)
                        .status(200)
                        .build());
    }

    @GetMapping("/api/v1/group-messages/{senderId}/{groupId}")
    public ResponseEntity<ApiResponse<List<Message>>> findGroupMessages(
            @PathVariable("senderId") String senderId,
            @PathVariable("groupId") String groupId
    ) {
        List<Message> messages = messageService.findMessagesByChatId(senderId,groupId);

        return ResponseEntity.ok(
                ApiResponse.<List<Message>>builder()
                        .data(messages)
                        .success(true)
                        .status(200)
                        .build());
    }
}
