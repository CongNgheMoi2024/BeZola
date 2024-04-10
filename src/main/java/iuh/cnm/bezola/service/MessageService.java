package iuh.cnm.bezola.service;

import iuh.cnm.bezola.models.Message;
import iuh.cnm.bezola.repository.MessageRepository;
import iuh.cnm.bezola.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;

    public Message save(Message message) {
        var chatId = roomService.getRoomId(message.getSenderId(), message.getRecipientId(), true)
                .orElseThrow(() -> new RuntimeException("Cannot create chatId"));
        message.setChatId(chatId);
        messageRepository.save(message);
        return message;
    }

    public List<Message> findMessages(String senderId, String recipientId) {
        var chatId = roomService.getRoomId(senderId, recipientId, false);
        List<Message> messages = chatId.map(messageRepository::findAllByChatId).orElse(new ArrayList<>());
        List<Message> result = new ArrayList<>();
        messages.forEach(message -> {
            if(message.getDeletedUsers()== null || !message.getDeletedUsers().contains(senderId)){
                result.add(message);
            }
        });
        return result;
    }
    public List<Message> findImageVideoMessages(String senderId, String recipientId) {
        var chatId = roomService.getRoomId(senderId, recipientId, false);
        List<Message> messages = chatId.map(messageRepository::findAllByChatIdAndMessageType).orElse(new ArrayList<>());
        List<Message> result = new ArrayList<>();
        messages.forEach(message -> {
            if(message.getDeletedUsers()== null || !message.getDeletedUsers().contains(senderId)){
                result.add(message);
            }
        });
        return result;
    }

    public List<Message> findFileMessages(String senderId, String recipientId) {
        var chatId = roomService.getRoomId(senderId, recipientId, false);
        List<Message> messages = chatId.map(messageRepository::findAllByChatIdAndFile).orElse(new ArrayList<>());
        List<Message> result = new ArrayList<>();
        messages.forEach(message -> {
            if(message.getDeletedUsers()== null || !message.getDeletedUsers().contains(senderId)){
                result.add(message);
            }
        });
        return result;
    }
    public Message findById(String id) {
        return messageRepository.findById(id).orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public void recallMessage(String messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        messageRepository.delete(message);
    }



    public void deleteMessage(String userId,String messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        message.getDeletedUsers().add(userId);
        messageRepository.save(message);
    }
}
