package com.imperialgrand.backend.authentication;

import com.imperialgrand.backend.auth.AuthController;
import com.imperialgrand.backend.authentication.DTO.records.*;
import com.imperialgrand.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

    private final AuthServiceV1 service;
    private final Logger logger = Logger.getLogger(AuthControllerV1.class.getName());

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signup(@RequestBody UserSignupRequest user){
        logger.info("AuthControllerV1: " + user.name());
        // Create the user account
        SignUpResponse response = service.createAccount(user);
        return ResponseEntity.ok(new ApiResponse(response, "Request Success."));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VerifyResponse>> verify(@RequestBody VerifyRequest req){
        VerifyResponse response = service.verifyOtp(req);
        return ResponseEntity.ok(new ApiResponse(response, "OTP verification success.."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<SignUpResponse>> resendOtp(@RequestBody ResendOtpRequest req){
        SignUpResponse response = service.resendOtp(req);
        return ResponseEntity.ok(new ApiResponse(response, "OTP resend request success."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody UserLoginRequest loginReq, HttpServletRequest servlet){
        Map<String, Object> mapBody = service.logUserIn(loginReq.email(), loginReq.password(), servlet.getHeader("x-device-id"));
        String accessToken  = (String) mapBody.get("access_token");
        String refreshToken = (String) mapBody.get("refresh_token");
        String deviceId = (String) mapBody.get("device_id");


        ResponseCookie refresh = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(false) // true for production, false for dev
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        ResponseCookie access = ResponseCookie.from("access-token", accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();

        ResponseCookie device = ResponseCookie.from("device-id", deviceId)
                .httpOnly(false)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        Map<String, Object> mapBodyResponse = new HashMap<>();
        mapBodyResponse.put("username", mapBody.get("username"));
        mapBodyResponse.put("role", mapBody.get("role"));

        logger.info(String.format("\n\nLogin response sending...\n Refresh-Token:%s\nAccess-Token:%s\nDevice-Id:%s", accessToken, refreshToken, deviceId));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString(), access.toString(), device.toString())
                .body(new ApiResponse<>(mapBodyResponse, "User logged in successfully!"));
    }

}
