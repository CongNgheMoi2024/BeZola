package iuh.cnm.bezola.service;

import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public void addFriend(String id, String friendId) throws DataNotFoundException, DataAlreadyExistsException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with id: " + id);
        }

        // Check if the friend ID exists
        Optional<User> optionalFriend = userRepository.findById(friendId);
        if (optionalFriend.isEmpty()) {
            throw new DataNotFoundException("Friend not found with id: " + friendId);
        }

        User user = optionalUser.get();

        if (user.getFriends() == null) {
            user.setFriends(new ArrayList<>());
        }

        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);

            userRepository.save(user);
        } else {
            throw new DataAlreadyExistsException("Friend already added with id: " + friendId);
        }
    }
}
