package iuh.cnm.bezola.repository;

import iuh.cnm.bezola.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findAllByChatId(String chatId);
}
