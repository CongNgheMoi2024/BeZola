package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.LoginDTO;
import iuh.cnm.bezola.dto.RefreshTokenDTO;
import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.LoginResponse;
import iuh.cnm.bezola.responses.RegisterResponse;
import iuh.cnm.bezola.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody UserDto userDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(errorMessages.toString())
                                .build()
                );
            }
            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message("Password not match")
                                .build()
                );
            }
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .message("Register success")
                            .user(user)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    RegisterResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponse loginResponse = userService.login(loginDTO.getPhone(), loginDTO.getPassword(),
                    String.valueOf(loginDTO.getRoleId() == null ? 1 : loginDTO.getRoleId()));
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message("Login failed: " + e.getMessage())
                    .build());
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenDTO refreshTokenDTO){
        try {
            return ResponseEntity.ok(userService.refreshToken(refreshTokenDTO));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message("Refresh token failed: "+e.getMessage())
                    .build());
        }
    }
}
