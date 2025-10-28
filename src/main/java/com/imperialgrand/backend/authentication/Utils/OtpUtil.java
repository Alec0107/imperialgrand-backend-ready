package com.imperialgrand.backend.authentication.Utils;

import com.imperialgrand.backend.authentication.DTO.records.VerifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class OtpUtil {

    @Value("${otp.secret}")
    private String secret;
    private final SecureRandom rand = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();
    private final PasswordEncoder passwordEncoder;

    public String generate(){
        int code = rand.nextInt(1_000_000);
        return String.format("%06d", code);
    }


    public String hash(String otp){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(otp.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public boolean match(String incomingOtpHash, String storedOtpHash) {
        return slowEquals(incomingOtpHash, storedOtpHash);
    }

    private boolean slowEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
