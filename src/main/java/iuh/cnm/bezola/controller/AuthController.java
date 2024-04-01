package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.LoginDTO;
import iuh.cnm.bezola.dto.RefreshTokenDTO;
import iuh.cnm.bezola.dto.UserDto;
import iuh.cnm.bezola.models.OTP;
import iuh.cnm.bezola.models.User;
import iuh.cnm.bezola.responses.ApiResponse;
import iuh.cnm.bezola.responses.LoginResponse;
import iuh.cnm.bezola.service.AuthService;
import iuh.cnm.bezola.utils.OTPQueue;
import iuh.cnm.bezola.utils.OTPRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> createUser(@Valid @RequestBody UserDto userDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error(errorMessages.toString())
                                .status(400)
                                .success(false)
                                .build()
                );
            }
            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {

                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Password not match")
                                .status(400)
                                .success(false)
                                .build()
                );
            }
            if (!OTPQueue.phoneNumbers.contains(userDTO.getPhone().replaceFirst("0","+84"))) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .error("Phone number is not verified")
                                .status(400)
                                .success(false)
                                .build()
                );
            }
            User user = userService.createUser(userDTO);
            LoginDTO loginDTO = LoginDTO.builder().phone(user.getPhone()).password(userDTO.getPassword()).build();
            return login(loginDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .error(e.getMessage())
                            .status(400)
                            .success(false)
                            .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponse loginResponse = userService.login(loginDTO.getPhone(), loginDTO.getPassword(),
                    loginDTO.getRoleId() == null ? "1" : loginDTO.getRoleId());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(loginResponse)
                            .message("Login success")
                            .status(200)
                            .success(true)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .error(e.getMessage())
                    .status(401)
                    .success(false)
                    .build());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        try {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .data(userService.refreshToken(refreshTokenDTO))
                            .message("Refresh token success")
                            .status(200)
                            .success(true)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .error(e.getMessage())
                    .status(401)
                    .success(false)
                    .build());
        }
    }
}
