package iuh.cnm.bezola.service;

import iuh.cnm.bezola.dto.ChangePasswordDTO;
import iuh.cnm.bezola.dto.ForgotPasswordDTO;
import iuh.cnm.bezola.exceptions.DataAlreadyExistsException;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByPhone(String phone) throws UserException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new UserException("User not found with phone: " + phone);
        }
        return optionalUser.get();
    }

    public User updateUser(String phone, ForgotPasswordDTO forgotPasswordDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + phone);
        }
        User user = optionalUser.get();
        if(!forgotPasswordDTO.getConfirmPassword().equals(forgotPasswordDTO.getPassword())) {
            throw new DataNotFoundException("Password not match");
        }
        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getPassword()));
        return userRepository.save(user);
    }

    public User changePassword(String phone, ChangePasswordDTO changePasswordDTO) throws DataNotFoundException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("User not found with phone: " + phone);
        }
        User user = optionalUser.get();
        if(!passwordEncoder.matches(changePasswordDTO.getOldPassword(),user.getPassword())) {
            throw new DataNotFoundException("Old password not match");
        }
        if(!changePasswordDTO.getConfirmPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new DataNotFoundException("Password not match");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        return userRepository.save(user);
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
