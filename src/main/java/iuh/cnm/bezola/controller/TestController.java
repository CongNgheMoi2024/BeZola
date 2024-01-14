package iuh.cnm.bezola.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}")
public class TestController {
    //Test heartbeat
    @GetMapping("/heartbeat")
    public String heartbeat() {
        return "OK";
    }
}
