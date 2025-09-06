package com.imperialgrand.backend.contact_us;


import com.imperialgrand.backend.contact_us.dto.ContactUs;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender mailSender;

    public boolean sendContactMessage(ContactUs contactUs) {

        try {
            var mm = mailSender.createMimeMessage();
            var h = new MimeMessageHelper(mm, false,"UTF-8");
            h.setFrom("contact@imperialgrandsg.com");
            h.setTo("contact@imperialgrandsg.com");
            h.setReplyTo(contactUs.getEmail());
            h.setSubject("[New Contact] From: " + contactUs.getEmail() + " | Subject: " + contactUs.getSubject());
            h.setText("""
                    New contact form submission
                    
                    Name: %s
                    Email: %s
                    
                    Message: %s
                    """.formatted(contactUs.getName(), contactUs.getEmail(), contactUs.getMessage()));
            mailSender.send(mm);
            return true;
        } catch (MessagingException e) {
            System.out.println("Email send error" + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Email send error" + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("sendContactMessage failed:");
            e.printStackTrace();   // <— this shows 535/530/553 etc.
            return false;
        }
    }



    public boolean senAcknowledgementEmail(ContactUs contactUs) {
        try {
            var mm = mailSender.createMimeMessage();
            // true = multipart so we can send plain + HTML
            var h = new MimeMessageHelper(mm, true, "UTF-8");

            // MUST be your Zoho login email (same as spring.mail.username)
            h.setFrom(new InternetAddress(sender, "Imperial Grand Cantonese Cuisine "));

            // send to the customer
            h.setTo(contactUs.getEmail());

            // if they reply, it goes to your inbox
            h.setReplyTo(sender);

            h.setSubject("[Imperial Grand] We’ve received your message");

            String plainText = """
                Hi %s,

                Thanks for reaching out to Imperial Grand Cantonese Cuisine.
                We've received your message and will get back to you shortly.

                Your Details
                -------------
                Name: %s
                Email: %s

                Your message:
                %s

                Just reply to this email if you need to add anything.

                Warm regards,
                Imperial Grand Team
                """.formatted(
                    contactUs.getName(),
                    contactUs.getName(),
                    contactUs.getEmail(),
                    contactUs.getMessage()
            );

            String html = """
                <div style="font-family:Arial,sans-serif;line-height:1.5; 
                padding:20px; 
                border: 1px solid black; 
                border-radius:6px;">
                  <h2>Thank you, %s!</h2>
                  <p>We’ve received your message and will get back to you shortly.</p>

                  <p><strong>Your Details</strong></p>
                  <ul>
                    <li><strong>Name:</strong> %s</li>
                    <li><strong>Email:</strong> %s</li>
                  </ul>

                  <p><strong>Your message:</strong></p>
                  <blockquote style="border-left:3px solid #ccc;margin:0;padding-left:10px;">
                    %s
                  </blockquote>

                  <p>If you need to add anything, just reply to this email — it will reach our team.</p>

                  <p style="color:#888;font-size:12px;margin-top:20px;">
                    Imperial Grand Cantonese Cuisine · Singapore
                  </p>
                </div>
                """.formatted(
                    contactUs.getName(),
                    contactUs.getName(),
                    contactUs.getEmail(),
                    contactUs.getMessage()
            );

            h.setText(plainText, html);
            mailSender.send(mm);
            return true;

        } catch (Exception e) {
            System.out.println("Ack email send error: " + e.getMessage());
            return false;
        }
    }



}
