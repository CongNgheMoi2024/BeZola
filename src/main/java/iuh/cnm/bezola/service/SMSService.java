package iuh.cnm.bezola.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import iuh.cnm.bezola.models.SMS;
import iuh.cnm.bezola.utils.OTPQueue;
import iuh.cnm.bezola.utils.OTPRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;

@Service
public class SMSService {
    private final String ACCOUNT_SID = "AC00d8c318991e2b93f2c1e6dda2547458";
    private final String AUTH_TOKEN = "fed61f7c6c17c117b299a97bbd872db7";
    private final String FROM_NUMBER = "+16592177742";

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

        OTPQueue.enqueue(new OTPRequest(otp, sms.getPhoneNo()));
    }

    public void recive(MultiValueMap<String,String> smsCallback){

    }
}
