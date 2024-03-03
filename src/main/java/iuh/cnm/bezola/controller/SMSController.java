package iuh.cnm.bezola.controller;

import iuh.cnm.bezola.dto.ForgotPasswordDTO;
import iuh.cnm.bezola.exceptions.DataNotFoundException;
import iuh.cnm.bezola.models.OTP;
import iuh.cnm.bezola.models.SMS;
import iuh.cnm.bezola.models.StoreOTP;
import iuh.cnm.bezola.service.SMSService;
import iuh.cnm.bezola.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("${api.prefix}")
public class SMSController {

    @Autowired
    private SMSService smsService;
    @Autowired
    private SimpMessagingTemplate webSocket;

    @Autowired
    private UserService userService;

    private final String TOPIC_DESTINATION = "/lesson/sms";

    @PostMapping("/phoneNumber")
    public ResponseEntity<String> smsSubmit(@RequestBody SMS sms){
        try {
            smsService.send(sms);
        }catch (Exception e){
            return new ResponseEntity<>("Something problem!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() +  ": SMS has been sent!: "+ sms.getPhoneNo());
        return new ResponseEntity<>("OTP send successfully", HttpStatus.OK);
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> sms(@RequestBody SMS sms){
        try {
            smsService.send(sms);
        }catch (Exception e){
            return new ResponseEntity<>("Something problem!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() +  ": SMS has been sent!: "+ sms.getPhoneNo());
        return new ResponseEntity<>("OTP send successfully", HttpStatus.OK);
    }
    @PutMapping("/otp-forget/{phone}")
    public ResponseEntity<?> verifyForget(@PathVariable String phone,@RequestBody ForgotPasswordDTO updatePasswordDTO) throws DataNotFoundException {
        if(updatePasswordDTO.getOtp() == StoreOTP.getOtp()) {
            userService.updateUser(phone,updatePasswordDTO);
            return ResponseEntity.ok("Verified forget password OTP successfully");
        }
        return ResponseEntity.badRequest().body("OTP not verified");
    }

    @PostMapping("/otp")
    public ResponseEntity<?> verifyOTP(@RequestBody OTP otp) {
        if(otp.getOtp() == StoreOTP.getOtp()) {
            return ResponseEntity.ok("OTP verified");
        }
        return ResponseEntity.badRequest().body("OTP not verified");
    }

    @RequestMapping(value = "/smscallback",method = RequestMethod.POST,consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void smsCallback(@RequestBody MultiValueMap<String,String> map){
        smsService.recive(map);
        webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() +  ": SMS has made a callback request!: "+ map.toString());
    }

    private String getTimeStamp(){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    }
}
