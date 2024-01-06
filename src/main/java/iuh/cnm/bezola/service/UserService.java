package iuh.cnm.bezola.service;

import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }
}
