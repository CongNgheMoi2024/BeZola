package iuh.cnm.bezola.service;

import iuh.cnm.bezola.dto.RefreshTokenDTO;
import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.exceptions.InvalidParamException;
import iuh.cnm.bezola.exceptions.PermissionDenyException;
import iuh.cnm.bezola.models.Role;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.repository.RoleRepository;
import iuh.cnm.bezola.repository.UserRepository;
import iuh.cnm.bezola.responses.LoginResponse;
import iuh.cnm.bezola.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User createUser(UserDto userDTO) throws Exception {
        String phone = userDTO.getPhone();
        if (userRepository.existsByPhone(phone))
            throw new DataIntegrityViolationException("Phone number already exists");
        Role role = roleRepository.findById(userDTO.getRoleId()).orElseThrow(() ->
                new DataNotFoundException("Role not found with id: " + userDTO.getRoleId()));
        if (role.getName().equals(Role.ADMIN)) {
            throw new PermissionDenyException("You don't have permission to create admin account");
        }
        User newUser = User.builder()
                .name(userDTO.getName())
//                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
//                .avatar(userDTO.getAvatar())
                .sex(userDTO.isSex())
                .birthday(userDTO.getBirthday())
//                .onlineStatus(userDTO.isOnlineStatus())
                .build();
        newUser.setRole(role);
        String password = userDTO.getPassword();
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);
        return userRepository.save(newUser);
    }

    public LoginResponse login(String phone, String password, String roleID) throws DataNotFoundException, InvalidParamException {
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phone number/password");
        }
        User existingUser = optionalUser.get();
        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new DataNotFoundException("Invalid phone number/password");
        }
        Optional<Role> optionalRole = roleRepository.findById(roleID);
        if (optionalRole.isEmpty() || !roleID.equals(existingUser.getRole().getId())) {
            throw new DataNotFoundException("Role does not exist");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phone, password, existingUser.getAuthorities());
        authenticationManager.authenticate(authenticationToken);

        existingUser.setOnlineStatus(true);
        userRepository.save(existingUser);

        return LoginResponse.builder()
                .accessToken(jwtTokenUtil.generateToken(existingUser))
                .refreshToken(jwtTokenUtil.generateRefreshToken(new HashMap<>(), existingUser))
                .user(existingUser)
                .phone(existingUser.getPhone())
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenDTO refreshTokenDTO) throws DataNotFoundException, InvalidParamException {
        String phone = jwtTokenUtil.extractPhone(refreshTokenDTO.getRefreshToken());
        Optional<User> optionalUser = userRepository.findByPhone(phone);
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phone number/password");
        }
        User existingUser = optionalUser.get();
        if (!jwtTokenUtil.validateToken(refreshTokenDTO.getRefreshToken(), existingUser)) {
            throw new DataNotFoundException("Invalid phone number/password");
        }
        return LoginResponse.builder()
                .accessToken(jwtTokenUtil.generateToken(existingUser))
                .refreshToken(refreshTokenDTO.getRefreshToken())
                .user(existingUser)
                .phone(existingUser.getPhone())
                .build();
    }
}
