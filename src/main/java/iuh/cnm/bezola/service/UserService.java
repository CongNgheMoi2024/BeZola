package iuh.cnm.bezola.service;

import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User createUser(UserDto userDto) {
        return userRepository.save(
                User.builder()
                        .name(userDto.getName())
                        .email(userDto.getEmail())
                        .password(userDto.getPassword())
                        .phone(userDto.getPhone())
                        .avatar(userDto.getAvatar())
                        .sex(userDto.isSex())
                        .birthday(userDto.getBirthday())
                        .onlineStatus(userDto.isOnlineStatus())
                        .build()
        );
    }
}
