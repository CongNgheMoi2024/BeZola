package iuh.cnm.bezola.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OTPRequest {
    private int otp;
    private String phoneNo;
    private Instant startTime;

    public OTPRequest(int otp, String phoneNo) {
        this.otp = otp;
        this.phoneNo = phoneNo;
        this.startTime = Instant.now();
    }
}
