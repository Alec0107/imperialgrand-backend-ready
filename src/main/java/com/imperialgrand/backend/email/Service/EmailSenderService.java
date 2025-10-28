package com.imperialgrand.backend.email.Service;

import com.imperialgrand.backend.auth.dto.RegisterRequest;
import com.imperialgrand.backend.common.utils.MaskUserEmail;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    @Value("${spring.mail.from:no-reply@imperialgrandsg.com}")
    private String fromAddress;

    @Value("${app.security.otp.minutes:15}")
    private String otpMinutes;

    @Value("${app.urls.reset-password:http://localhost:5500/pages/forgot-password/reset-password.html}")
    private String resetUrl;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpViaEmail(String toEmail, String otp) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariables(Map.of(
                    "otp", otp,
                    "minutes", otpMinutes
            ));

            String html = templateEngine.process("otp", context);

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime, true, StandardCharsets.UTF_8.name()
            );
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your Verification Code");
            helper.setText(html, true);

            mailSender.send(mime);
            log.info("✅ OTP email sent to: {}", toEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Unable to send OTP email", ex);
        }
    }

    public String sendSimpleEmailVerif(RegisterRequest registerRequest, String token, int tokenId) {
        return sendVerificationEmail(registerRequest.getEmail(), registerRequest.getFirstName(), token, tokenId);
    }

    private String sendVerificationEmail(String toEmail, String firstName, String token, int tokenId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("[Imperial Grand] Verify Your Imperial Grand Account");
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "Welcome to Imperial Grand Cantonese Restaurant!\n" +
                            "To finish your registration, please verify your email by clicking this link:\n" +
                            "http://192.168.0.112:8080/api/v1/auth/verify?token=" + token + "&id=" + tokenId + "\n\n" +
                            "Unable to click the link above? You can also copy and paste the link into your browser."
            );

            mailSender.send(message);

            String maskedEmail = MaskUserEmail.maskUserEmail(toEmail);
            return "We sent a verification email link to " + maskedEmail + ". Please verify email to activate your account.";
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Unable to send verification email", ex);
        }
    }

    public String sendResetPasswordEmail(String toEmail, String firstName, String token, int tokenId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("[Imperial Grand] Reset Your Password");
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "We received a request to reset the password for your account.\n\n" +
                            "Click the link below to reset your password:\n\n" +
                            resetUrl + "?token=" + token + "&id=" + tokenId + "\n\n" +
                            "If you did not request this, you can safely ignore this email — your password will remain unchanged.\n\n" +
                            "This link will expire in " + otpMinutes + " minutes for your security.\n\n" +
                            "Thanks,\nThe Imperial Grand Team"
            );

            mailSender.send(message);
            String maskedEmail = MaskUserEmail.maskUserEmail(toEmail);
            return "If an account exists for " + maskedEmail + ", a password reset link has been sent.";
        } catch (Exception ex) {
            log.error("Failed to send reset email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new RuntimeException("Unable to send reset email", ex);
        }
    }

    public void testEmail(String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo("melrich.npl@gmail.com");
        message.setSubject("[Imperial Grand] Reset Your Password");
        message.setText("Hi! This is your otp: " + otp);
        mailSender.send(message);
    }
}