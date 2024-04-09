package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findAllByChatId(String chatId);

    @Query("{ 'chatId' : ?0, 'type' : { $in: ['IMAGE', 'VIDEO'] } }")
    List<Message> findAllByChatIdAndMessageType(String chatId);

    @Query("{ 'chatId' : ?0, 'type' : { $in: ['FILE'] } }")
    List<Message> findAllByChatIdAndFile(String chatId);
}
