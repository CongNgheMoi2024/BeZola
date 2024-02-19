package iuh.cnm.bezola.service;

import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUserByPhone(String phone) throws UserException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new UserException("User not found with phone: " + phone);
        }
        return optionalUser.get();
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
