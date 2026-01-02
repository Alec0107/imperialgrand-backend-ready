package com.imperialgrand.backend.authentication;

import com.imperialgrand.backend.UserDashboard.ReservationDetailsDTO;
import com.imperialgrand.backend.authentication.DTO.AccountDetailsDTO;
import com.imperialgrand.backend.authentication.DTO.records.*;
import com.imperialgrand.backend.authentication.Exception.OtpVerificationException;
import com.imperialgrand.backend.authentication.Repository.Status;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.authentication.Repository.UserAccountRepositoryService;
import com.imperialgrand.backend.authentication.Utils.OtpUtil;
import com.imperialgrand.backend.common.utils.MaskUserEmail;
import com.imperialgrand.backend.email.Service.EmailSenderService;
import com.imperialgrand.backend.email.utils.HashTokenUtils;
import com.imperialgrand.backend.jwt.JwtGeneratorService;
import com.imperialgrand.backend.jwt.JwtRepositoryService;
import com.imperialgrand.backend.jwt.exception.InvalidJwtTokenException;
import com.imperialgrand.backend.jwt.exception.MissingRefreshTokenException;
import com.imperialgrand.backend.jwt.exception.RefreshTokenExpiredException;
import com.imperialgrand.backend.jwt.exception.WrongTypeTokenException;
import com.imperialgrand.backend.jwt.model.JwtToken;
import com.imperialgrand.backend.jwt.repository.JwtTokenRepository;
import com.imperialgrand.backend.redis.OtpRedis;
import com.imperialgrand.backend.user.exception.EmailNotFoundException;
import com.imperialgrand.backend.user.exception.EmailNotVerifiedException;
import com.imperialgrand.backend.user.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceV1 {

    private final EmailSenderService emailService;
    private final UserAccountRepositoryService userRepoService;
    private final JwtRepositoryService jwtRepoService;
    private final JwtGeneratorService jwtGenService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = Logger.getLogger(AuthServiceV1.class.getName());

    private final OtpUtil otpUtil;
    private final OtpRedis otpRedis;


    // Create an account
    public SignUpResponse createAccount(UserSignupRequest user){
        //1. Validate input (name, email, password, phone number, etc.).

        //2. Hash the password using BCryptPasswordEncoder.
        String hashedPassword = passwordEncoder.encode(user.password());
        System.out.println(hashedPassword);

        //3. Create a new User entity and save in the database (status: UNVERIFIED).
        User userEntity = User.builder()
                .name(user.name())
                .email(user.email())
                .password(hashedPassword)
                .phone(user.phoneNumber())
                .emailVerified(false)
                .status(Status.UNVERIFIED)
                .createdAt(Instant.now())
                .role(Role.USER)
                .build();

        logger.info(userEntity.toString());
        userRepoService.saveUserAccount(userEntity);

        //4. Generate and Hash OTP via OTPService
        String otp = otpUtil.generate();
        String hash = otpUtil.hash(otp);
        logger.info("HASHED OTP: " + hash);

        //5. Save OTP in Redis Cache via RedisService
        String verifyId = UUID.randomUUID().toString();
        otpRedis.save(verifyId, user.email(), hash);

        //6. Send OTP via Email .
        emailService.sendOtpViaEmail(user.email(), otp);

        //7. Return a JSON response to the frontend.
        return new SignUpResponse(
                verifyId,
                user.email(),
                MaskUserEmail.maskUserEmail(user.email()),
                0,
                Instant.now().toEpochMilli(),
                "Verification code sent successfully. Please check your email."
        );
    }

    public VerifyResponse verifyOtp(VerifyRequest req){
        boolean isVerified = otpRedis.verify(req);
        if(isVerified){
            userRepoService.markUserAsVerified(req.email());
        }
        return new VerifyResponse(true, "Your email address is verified! You can now log in to your account.", "Login");
    }

    public SignUpResponse resendOtp(ResendOtpRequest req){
        // check if user's email is already verified
        Instant now = Instant.now();
        Instant expiresAt = Instant.ofEpochMilli(req.resendCooldownMs());

        if(now.isBefore(expiresAt)){
           logger.info("Cooling down...");
           throw new OtpVerificationException(HttpStatus.TOO_MANY_REQUESTS, "Cooling down...");
        }

        String email = req.email();
        otpRedis.checkVerifyIdInCache(req.verifyId());

        //4. Generate and Hash OTP via OTPService
        String otp = otpUtil.generate();
        String hash = otpUtil.hash(otp);
        logger.info("HASHED OTP: " + hash);

        //5. Save OTP in Redis Cache via RedisService
        String verifyId = UUID.randomUUID().toString();
        long resendMs = Instant.now().plus(Duration.ofSeconds(30)).toEpochMilli();
        otpRedis.save(verifyId,email, hash);

        //6. Send OTP via Email .
        emailService.sendOtpViaEmail(email, otp);

        return new SignUpResponse(
                verifyId,
                email,
                MaskUserEmail.maskUserEmail(email),
                0,
                resendMs,
                "We’ve just sent you another code. It might take a minute to arrive. Don’t forget to check your spam or junk folder.”"
        );
    }

    // Login
    public Map<String, Object> logUserIn(String email, String password, String  incomingDeviceId){
        logger.info(String.format("LOGIN\nEmail:%s\nPassword:%s\nDeviceId:%s", email, password, incomingDeviceId));
        String did = incomingDeviceId == null ? null : incomingDeviceId.trim();
        boolean missingDeviceId =
                did == null
                        || did.isEmpty()
                        || "null".equalsIgnoreCase(did)
                        || "undefined".equalsIgnoreCase(did);
        Map<String, Object> loginResponse = new HashMap<>();
        String accessToken = null;
        String refreshToken = null;
        String deviceId = null;
        User user  = userRepoService.readUserByEmail(email);
        if(user == null){
            throw new EmailNotFoundException("No account associated with this email. Please contact support or sign up.");
        }
        if(!user.isEmailVerified()){
            throw new EmailNotVerifiedException("Email is not verified");
        }
        if(missingDeviceId){
            deviceId = UUID.randomUUID().toString();
            logger.info(String.format("\nNew user is logging in: \nDevice id:%s", deviceId));
        }else{
            logger.info(String.format("\nOld user is logging in: \nDevice id:%s", incomingDeviceId));
            JwtToken previousRT = jwtRepoService.getTokenByUserIdAndDeviceId(user.getId(), incomingDeviceId);
            previousRT.setRevoked(true);
            jwtRepoService.saveOldToken(previousRT);
            deviceId = incomingDeviceId;
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        // generate a new refresh and access jwt token
        refreshToken = jwtGenService.generateRefreshToken(user, Duration.ofDays(7));
        accessToken  = jwtGenService.generateAccessToken(user, Duration.ofMinutes(10));
        String salt = HashTokenUtils.generateSalt();
        String hashedRefreshToken = HashTokenUtils.hashRefreshToken(refreshToken, salt);
        jwtRepoService.saveNewToken(hashedRefreshToken, salt, user, deviceId, LocalDateTime.now().plus(Duration.ofDays(7)));
        return helperResponseMap(user, accessToken, refreshToken, deviceId);
    }

    public static Map<String, Object> helperResponseMap(User user, String accessToken, String refreshToken, String deviceId) {
        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("user_id", user.getId());
        mapResponse.put("username", user.getUsername());
        mapResponse.put("name", user.getName());
        mapResponse.put("isVerified", user.isEmailVerified());
        mapResponse.put("role", user.getRole());
        mapResponse.put("access_token", accessToken);
        mapResponse.put("refresh_token", refreshToken);
        mapResponse.put("device_id", deviceId);
        return mapResponse;
    }

    public Map<String, Object> rotateRefreshToken(String rt, String deviceId) {
        logger.info("Processing refresh token generation request for user");
        logger.info("Refresh token: " + rt);
        logger.info("Device ID: " + deviceId);
        User user = null;
        try {
            // check if token is present
            if(rt == null || rt.isEmpty()) {
                throw new MissingRefreshTokenException("Refresh token is empty");
            }
            // check if token is refresh and is not tampered
            Claims claims = jwtGenService.extractAllClaims(rt);
            if(!claims.get("type").equals("refresh-token")){
                throw new WrongTypeTokenException("Wrong type of jwt token.");
            }
            // fetch the user and matching token from DB early
            String userEmail = claims.getSubject();
            user = userRepoService.findUserByEmail(userEmail);
            JwtToken matchingToken = jwtRepoService.getTokenByUserIdAndDeviceId(Long.valueOf(user.getId()), deviceId);
            String hashedIncomingRefreshToken = HashTokenUtils.hashRefreshToken(rt, matchingToken.getSalt());
            if(!hashedIncomingRefreshToken.equals(matchingToken.getToken())) throw new InvalidJwtTokenException("Refresh Token is not the same.");

            // DB expiration check should come before claims check
            if (matchingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                matchingToken.setRevoked(true);
                jwtRepoService.saveOldToken(matchingToken); // Save change if you want
                throw new RefreshTokenExpiredException("Refresh token has expired (DB check). Please login again.");
            }

            // Optional: now check JWT claim expiration as secondary (for redundancy/logging)
            if (claims.getExpiration().before(new Date())) {
                throw new RefreshTokenExpiredException("Refresh token has expired (JWT claims). Please login again.");
            }

            // revoke the matched token
            matchingToken.setRevoked(true);
            jwtRepoService.saveOldToken(matchingToken);

            //Start generating new Refresh and Access
            String newAccessToken = null;
            String newRefreshToken = null;
            String previousDeviceId = matchingToken.getDeviceId();

            // generate a new refresh and access jwt token
            newRefreshToken = jwtGenService.generateRefreshToken(user, Duration.ofDays(7));
            newAccessToken  = jwtGenService.generateAccessToken(user, Duration.ofMinutes(10));

            String newSalt = HashTokenUtils.generateSalt();
            String newHashedRefreshToken = HashTokenUtils.hashRefreshToken(newRefreshToken, newSalt);

            jwtRepoService.saveNewToken(newHashedRefreshToken, newSalt, user, deviceId, LocalDateTime.now().plus(Duration.ofDays(7)));
            System.out.println("REFRESH TOKEN CREATED AT: " + new Date());
            System.out.println("REFRESH NEW: (Refresh-token): " + newRefreshToken);
            return helperResponseMap(user, newAccessToken, newRefreshToken, previousDeviceId);

        } catch (JwtException e) {
            throw new InvalidJwtTokenException("Invalid refresh token.");
        }
    }


    public AccountDetailsDTO getCurrentUser(User me){
       return userRepoService.getCurrent(me);
    }

    public void updateBirthday(User me, String birthday){
        userRepoService.setBirthday(me, birthday);
    }

}
