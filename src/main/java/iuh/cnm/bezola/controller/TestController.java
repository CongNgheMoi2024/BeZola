package iuh.cnm.bezola.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/heartbeat")
    public String heartbeat() {
        return "OK";
    }
}
