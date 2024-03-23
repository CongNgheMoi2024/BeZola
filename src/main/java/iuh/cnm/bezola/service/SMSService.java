package iuh.cnm.bezola.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import iuh.cnm.bezola.models.SMS;
import iuh.cnm.bezola.models.StoreOTP;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;

@Service
public class SMSService {
    private final String ACCOUNT_SID = "ACbf84ab2e93a5179669ecd15fd6c46951";
    private final String AUTH_TOKEN = "6d8e5f018c8f57ed94f08a2c93f3f60e";
    private final String FROM_NUMBER = "+13345083172";

    public void send(SMS sms) throws ParseException{
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        int min = 100000;
        int max = 999999;
        int otp = (int) (Math.random() * (max - min + 1) + min);

        Message message = Message.creator(
                new PhoneNumber(sms.getPhoneNo()),
                new PhoneNumber(FROM_NUMBER),
                "Your OTP is: " + otp)
                .create();

        StoreOTP.setOtp(otp);
    }

    public void recive(MultiValueMap<String,String> smsCallback){

    }
}
