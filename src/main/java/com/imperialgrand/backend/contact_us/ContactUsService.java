package com.imperialgrand.backend.contact_us;

import com.imperialgrand.backend.common.globalexception.CooldownException;
import com.imperialgrand.backend.contact_us.dto.ContactUs;
import com.imperialgrand.backend.contact_us.exception.InternalServerError;
import com.imperialgrand.backend.redis.rate_limiter.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactUsService {

    private final RateLimitService rateLimitService;
    private final MailService mailService;



    public void validateContactUsRequest(ContactUs contactUs, String clientIp){

        // 1. validate inputs
        validateInputs(contactUs);

        // 2. Rate limit check
        checkRateLimit(clientIp);

        // 3. save to DB + send mail

        // 4. send admin email (must succeed)
        System.out.println(contactUs);
        boolean wasEmailSend = mailService.sendContactMessage(contactUs);
        if(!wasEmailSend){
            System.err.println("Something wrong in sending email to company's email inbox");
            // throw an exception here 500
            throw new InternalServerError("Something wrong in sending email to company's email inbox");
        }

        boolean wasAcknowledgeSent = mailService.senAcknowledgementEmail(contactUs);
        if(!wasAcknowledgeSent){
            // log but don't fail the request
            System.err.println("Acknowledgement email failed for: " + contactUs.getEmail());
        }

    }


    public void validateInputs(ContactUs contactUs){
        // 1. Validations
        if (contactUs.getName() == null || contactUs.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (contactUs.getName().length() < 2 || contactUs.getName().length() > 50) {
            throw new IllegalArgumentException("Name must be 2–50 characters.");
        }

        if (contactUs.getEmail() == null || !contactUs.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        if (contactUs.getSubject() == null || contactUs.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (contactUs.getMessage() == null || contactUs.getMessage().length() < 10) {
            throw new IllegalArgumentException("Message must be at least 10 characters.");
        }
    }

    public void checkRateLimit(String clientIp){
        if (!rateLimitService.isRequestAllowed(clientIp)) {
            throw new CooldownException("Too many requests. Please try again later.");
        }
    }

}
