package com.imperialgrand.backend.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.auth.AuthService;
import com.imperialgrand.backend.authentication.DTO.records.VerifyRequest;
import com.imperialgrand.backend.authentication.Exception.OtpVerificationException;
import com.imperialgrand.backend.authentication.Exception.TooManyOtpAttemptException;
import com.imperialgrand.backend.authentication.Utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpRedis {

    private final Logger logger = Logger.getLogger(OtpRedis.class.getName());
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final OtpUtil otpUtil;

    public void save(String verifyId, String email, String hash){
        Instant now = Instant.now();
        Instant softExpiresAt = now.plus(Duration.ofMinutes(15));
        Instant hardExpiresAt = now.plus(Duration.ofMinutes(20));

        Map<String, Object> object = Map.of(
                "email", email,
                "otpHash", hash,
                "attempts", 0,
                "purpose", "SIGNUP",
                "softExpiresAtMs", softExpiresAt.toEpochMilli(),
                "verifyCooldown", now.toEpochMilli()
        );

        logger.info(String.format("\nRedis:\nEmail:%s\nHashedOTP:%s\nPurpose:%s\nOTP Exp:%s", email, hash,"Signup", softExpiresAt));

        String key = keyBuilder(verifyId);
        try{
            String json = mapper.writeValueAsString(object);
            redis.opsForValue().set(key, json, Duration.ofMinutes(20));
        }catch(JsonProcessingException ex){
            logger.info(ex.getMessage());
        }

    }

    public boolean verify(VerifyRequest req){
        // 1. Build the key
        String key = keyBuilder(req.verifyId());
        // 2. Hash the otp sent by frontend
        String incomingOtpHash =  otpUtil.hash(req.otp());
        // 3. Get the object json string from redis
        String json = redis.opsForValue().get(key);

        // 4. Convert the json string to a java record (VerifyRequest)
        if (json == null) throw new OtpVerificationException(HttpStatus.NOT_FOUND, "Invalid OTP. Please try again.");

            try{
                Map<String, Object> m = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
                String emailStored = m.get("email").toString();
                String storedOtpHash = m.get("otpHash").toString();
                long softExpiresAT = (long) m.get("softExpiresAtMs");
                long cooldown = (long) m.get("verifyCooldown");
                int attempts = (int) m.get("attempts");
                Instant now = Instant.now();

                // 5. Check if expired
                if(now.toEpochMilli() >= softExpiresAT){
                    logger.info("OTP Expired");
                    throw new OtpVerificationException(HttpStatus.GONE, "Code expired. Please request a new code to continue.");
                }

                // 6. Check the email
                if(!emailStored.equals(req.email())) {
                    logger.info("Email mismatch");
                    throw new OtpVerificationException(HttpStatus.BAD_REQUEST, "Email mismatch");
                }

                Instant expiresAt = Instant.ofEpochMilli(cooldown);
                if(now.isBefore(expiresAt)){
                     logger.info("Attempt in cooldown: " + attempts);
                     logger.info("cooldown");
                    throw new TooManyOtpAttemptException(HttpStatus.TOO_MANY_REQUESTS, "You’re in a cooldown period — try again shortly.", cooldown);
                }

//                if(cooldown >= now){
//                    long cooldownRemaining = cooldown - now;
//                    logger.info("Cooldown left: " + cooldownRemaining);
//                    logger.info("Attempt in cooldown: " + attempts); //!
//                    logger.info("cooldown");
//                    throw new TooManyOtpAttemptException(HttpStatus.TOO_MANY_REQUESTS, "You’re in a cooldown period — try again shortly.", cooldownRemaining);
//                }

                // 7. Check the attempts
                if(attempts >= 5){
                    logger.info("Checking attempts: " + attempts); //!
                    long retryAt = Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli();
                    m.put("verifyCooldown", retryAt);
                    m.put("attempts", 0);
                    String updated = mapper.writeValueAsString(m);   // JSON -> String
                    redis.opsForValue().set(key, updated);           // overwrite
                    throw new TooManyOtpAttemptException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts. Please wait 5 minutes before trying again or request a new code.", retryAt);
                }

                // 8. Check the incoming hash otp and the hash otp stored in redis
                boolean ok = otpUtil.match(incomingOtpHash, storedOtpHash);
                if(!ok) {
                    attempts++;
                    m.put("attempts", attempts);

                    String updated = mapper.writeValueAsString(m);   // JSON -> String
                    redis.opsForValue().set(key, updated);           // overwrite

                    logger.info("OTP mismatch" + attempts); //!
                    throw new OtpVerificationException(HttpStatus.BAD_REQUEST, "Incorrect code. Please check your email and try again.");
                }

                // 9. delete the key and return true to enable user
                redis.delete(key);
                return true;

            }catch(JsonProcessingException ex){
                logger.info("Error in converting json string to an object using object mapper");
                throw new OtpVerificationException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
            }
    }

    public void checkVerifyIdInCache(String verifyId){
        // 1. If verifyId is deleted or not in redis just send an email too
        if(redis.opsForValue().get(keyBuilder(verifyId)) != null){
            deleteKey(keyBuilder(verifyId));
        }
    }

    public void deleteKey(String key){
        redis.delete(key);
    }

    private String keyBuilder(String verifyId){
        return String.format("verify:otp:%s", verifyId);
    }
}
