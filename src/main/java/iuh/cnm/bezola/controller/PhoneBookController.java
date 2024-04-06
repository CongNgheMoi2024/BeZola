package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.PhoneBook;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/phone-books")
@RequiredArgsConstructor
public class PhoneBookController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> savePhoneBooks(@RequestHeader("Authorization") String jwt, @RequestBody List<PhoneBook> phoneBooks){
        try {
            userService.savePhoneBooks(jwt, phoneBooks);
        } catch (UserException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Post list phone book successfully");
    }

}
