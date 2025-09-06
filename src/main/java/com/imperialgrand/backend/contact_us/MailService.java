package com.imperialgrand.backend.contact_us;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imperialgrand.backend.contact_us.dto.ContactUs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MailService {

    private final RestTemplate http = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${zeptomail.api.url}")
    private String apiUrl;

    @Value("${zeptomail.api.key}")
    private String apiKeyRaw;

    @Value("${zeptomail.from.address}")
    private String fromAddress;

    @Value("${zeptomail.from.name:Imperial Grand}")
    private String fromName;

    private String apiKey() { return apiKeyRaw == null ? "" : apiKeyRaw.trim(); }

    public boolean sendContactMessage(ContactUs c) {
        String subject = "[New Contact] From: %s | Subject: %s".formatted(c.getEmail(), c.getSubject());

        String htmlBody = """
            <div>
              <h3>New contact form submission</h3>
              <p><b>Name:</b> %s<br/>
                 <b>Email:</b> %s</p>
              <p><b>Message:</b><br/>%s</p>
            </div>
        """.formatted(c.getName(), c.getEmail(), escapeHtml(c.getMessage()));

        EmailRequest payload = new EmailRequest(
                new From(fromAddress, fromName),
                List.of(new To(new EmailAddress("contact@imperialgrandsg.com", "Imperial Grand"))),
                subject,
                htmlBody
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            // IMPORTANT: scheme string exactly like your working curl
            headers.set("Authorization", "Zoho-enczapikey " + apiKey());

            // log what we actually send
            String json = mapper.writeValueAsString(payload);
            System.out.println("ZeptoMail request JSON: " + json);

            HttpEntity<String> req = new HttpEntity<>(json, headers);
            ResponseEntity<String> res = http.postForEntity(apiUrl, req, String.class);

            System.out.println("ZeptoMail status=" + res.getStatusCode() + " body=" + res.getBody());
            return res.getStatusCode().is2xxSuccessful();

        } catch (HttpStatusCodeException e) {
            System.err.println("ZeptoMail API error: " + e.getStatusCode() +
                    " body=" + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("ZeptoMail API error: " + e.getMessage());
            return false;
        }
    }

    private static String escapeHtml(String s) {
        return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    // DTOs shaped exactly like curl body
    @Data @NoArgsConstructor @AllArgsConstructor
    static class EmailRequest {
        private From from;
        private List<To> to;
        private String subject;
        @JsonProperty("htmlbody")
        private String htmlBody;
    }
    @Data @NoArgsConstructor @AllArgsConstructor static class From { private String address; private String name; }
    @Data @NoArgsConstructor @AllArgsConstructor static class To   { @JsonProperty("email_address") private EmailAddress emailAddress; }
    @Data @NoArgsConstructor @AllArgsConstructor static class EmailAddress { private String address; private String name; }
}
