package com.imperialgrand.backend.authentication;

import com.imperialgrand.backend.auth.AuthController;
import com.imperialgrand.backend.authentication.DTO.AccountDetailsDTO;
import com.imperialgrand.backend.authentication.DTO.UpdateBirthdayRequest;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.authentication.DTO.records.*;
import com.imperialgrand.backend.common.response.ApiResponse;
import com.imperialgrand.backend.config.SecurityConfig;
import com.imperialgrand.backend.user.model.Role;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
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

        logger.info(String.format("\n\nLogin response sending...\n Refresh-Token:%s\nAccess-Token:%s\nDevice-Id:%s", refreshToken, accessToken, deviceId));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString(), access.toString(), device.toString())
                .body(new ApiResponse<>(mapBodyResponse, "User logged in successfully!"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> meResponse(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        // user is guaranteed non-null here because the filter authenticated the request
        MeResponse mapResponseBody = new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified()
        );

        logger.info("User is authenticated successfully using access token");

        return ResponseEntity
                .ok()
                .body(new ApiResponse<>(mapResponseBody, "User is authenticated successfully using access token"));

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<MeResponse>> refreshToken(HttpServletRequest request){
        logger.info("Refresh endpoint hit.");
        logger.info("Device-ID: " + request.getHeader("x-device-id"));
        logger.info("Cookies: " + Arrays.toString(request.getCookies()));
        Map<String, Object> mapBody = new HashMap<>();

        String deviceId = request.getHeader("x-device-id");
        String incomingRefreshToken = getRefreshCookieToken(request);

        Map<String, Object> mapResponse = service.rotateRefreshToken(incomingRefreshToken, deviceId);
        String newAccessToken = (String) mapResponse.get("access_token");
        String newRefreshToken = (String) mapResponse.get("refresh_token");
        String deviceIdTest = (String)mapResponse.get("device_id");
        Long userId = (Long)mapResponse.get("user_id");
        String email = (String)mapResponse.get("username");
        String name = (String)mapResponse.get("name");
        Role role = (Role)mapResponse.get("role");
        boolean isEmailVerified = (boolean)mapResponse.get("isVerified");

        System.out.println("NEW REFRESH TOKEN: " + newRefreshToken);
        ResponseCookie refresh = ResponseCookie.from("refresh-token", newRefreshToken)
                .httpOnly(true)
                .secure(false) // true for production, false for dev
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        ResponseCookie access = ResponseCookie.from("access-token", newAccessToken)
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
        MeResponse object = new MeResponse(
                userId,
                email,
                name,
                role,
                isEmailVerified
        );
        logger.info(String.format("\n\nRefresh Token Rotation response...\nRefresh-Token:%s\nAccess-Token:%s\nDevice-Id:%s", newRefreshToken, newAccessToken, deviceId));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refresh.toString(), access.toString(), device.toString())
                .body(new ApiResponse<>(object, "Refresh token has been successfully rotated. New Refresh and Access token was sent."));
    }


        private String getRefreshCookieToken(HttpServletRequest request) {
        String incomingRefreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh-token")) {
                    incomingRefreshToken = cookie.getValue();
                    System.out.println("REFRESH TOKEN T: " + cookie.getValue());
                    break;
                }
            }
        }
        return incomingRefreshToken;
    }


    @GetMapping("/user/dashboard/account")
    public ResponseEntity<ApiResponse<AccountDetailsDTO>> me (@AuthenticationPrincipal User me){
        AccountDetailsDTO accDTO = service.getCurrentUser(me);
        return ResponseEntity.ok(new ApiResponse(accDTO, "Account Dashboard."));
    }

    @PutMapping("/user/dashboard/account/birthday")
    public ResponseEntity<ApiResponse<String>>setBirthday(@AuthenticationPrincipal User me, @RequestBody UpdateBirthdayRequest body){
        service.updateBirthday(me, body.birthday());
        return ResponseEntity.ok(new ApiResponse(null, "Success birthday update."));
    }

}
