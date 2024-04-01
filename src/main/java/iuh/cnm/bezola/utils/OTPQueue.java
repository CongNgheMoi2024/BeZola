package iuh.cnm.bezola.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class OTPQueue {
    private static Queue<OTPRequest> queue = new LinkedList<>();
//    5 minutes
    public static Set<String> phoneNumbers = new HashSet<>();
    private static final int TIMEOUT_MINUTES = 5;

    public static synchronized void enqueue(OTPRequest request) {
        queue.add(request);
    }

    public static synchronized OTPRequest dequeue() {
        return queue.poll();
    }
    public static synchronized boolean verifyOTP(OTPRequest otpRequest) {
        Instant currentTime = Instant.now();
        for (OTPRequest request : queue) {
            if (request.getOtp() == otpRequest.getOtp() && request.getPhoneNo().equals(otpRequest.getPhoneNo())) {
                // Kiểm tra thời gian timeout
                if (Duration.between(request.getStartTime(), currentTime).toMinutes() <= TIMEOUT_MINUTES) {
                    queue.remove(request); // Xóa yêu cầu sau khi xác nhận thành công
                    phoneNumbers.add(otpRequest.getPhoneNo());
                    return true;
                } else {
                    // Time out loại bỏ yêu cầu
                    queue.remove(request);
                    return false;
                }
            }
        }
        return false;
    }
}
