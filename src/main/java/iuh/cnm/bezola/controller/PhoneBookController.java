package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.exceptions.UserException;
import iuh.cnm.bezola.models.PhoneBook;
import iuh.cnm.bezola.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/phone-books")
@RequiredArgsConstructor
public class PhoneBookController {
    private final UserService userService;

    @PostMapping
    public void savePhoneBooks(@RequestHeader("Authorization") String jwt, @RequestBody List<PhoneBook> phoneBooks) throws UserException {
        userService.savePhoneBooks(jwt, phoneBooks);
    }

}
