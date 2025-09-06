package com.imperialgrand.backend.contact_us;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.imperialgrand.backend.contact_us.dto.ContactUs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MailService {

    private final RestTemplate http = new RestTemplate();

    @Value("${zeptomail.api.url}")
    private String apiUrl;

    @Value("${zeptomail.api.key}")
    private String apiKey;

    @Value("${zeptomail.from.address}")
    private String fromAddress;

    @Value("${zeptomail.from.name:Imperial Grand}")
    private String fromName;

    public boolean sendContactMessage(ContactUs c) {
        String subject = "[New Contact] From: %s | Subject: %s"
                .formatted(c.getEmail(), c.getSubject());

        String textBody = """
                New contact form submission

                Name: %s
                Email: %s

                Message:
                %s
                """.formatted(c.getName(), c.getEmail(), c.getMessage());

        EmailRequest payload = new EmailRequest(
                new From(fromAddress, fromName),
                List.of(new To(new EmailAddress("contact@imperialgrandsg.com", "Imperial Grand"))),
                subject,
                textBody
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Zoho-enczapikey " + apiKey);

            HttpEntity<EmailRequest> req = new HttpEntity<>(payload, headers);
            ResponseEntity<String> res = http.postForEntity(apiUrl, req, String.class);

            return res.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("ZeptoMail API error: " + e.getMessage());
            return false;
        }
    }

    // DTO classes (small + simple)
    @Data @NoArgsConstructor @AllArgsConstructor
    static class EmailRequest {
        private From from;
        private List<To> to;
        private String subject;
        @JsonProperty("textbody")
        private String textBody;
    }
    @Data @NoArgsConstructor @AllArgsConstructor static class From { private String address; private String name; }
    @Data @NoArgsConstructor @AllArgsConstructor static class To   { @JsonProperty("email_address") private EmailAddress emailAddress; }
    @Data @NoArgsConstructor @AllArgsConstructor static class EmailAddress { private String address; private String name; }
}
