package com.example.expanse_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${api.key.brevo}")
    private String brevoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    // ✅ USED BY NotificationService
    @Async
    public void sendEmail(String toEmail, String subject, String htmlBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        String body = """
        {
          "sender": {
            "name": "Money Manager",
            "email": "sauravkr81.93@gmail.com"
          },
          "to": [{
            "email": "%s"
          }],
          "subject": "%s",
          "htmlContent": "%s"
        }
        """.formatted(
                toEmail,
                subject,
                htmlBody.replace("\"", "\\\"") // escape quotes
        );

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BREVO_URL, request, String.class);
        } catch (Exception e) {
            // Never crash schedulers
            System.err.println("Brevo email failed: " + e.getMessage());
        }
    }

    // ✅ USED DURING REGISTRATION
    @Async
    public void sendActivationEmail(String toEmail, String toName, String activationLink) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        String body = """
        {
          "sender": {
            "name": "Money Manager",
            "email": "sauravkr81.93@gmail.com"
          },
          "to": [{
            "email": "%s",
            "name": "%s"
          }],
          "subject": "Activate your account",
          "htmlContent": "<p>Click below to activate your account:</p><a href='%s'>Activate Account</a>"
        }
        """.formatted(toEmail, toName, activationLink);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BREVO_URL, request, String.class);
        } catch (Exception e) {
            System.err.println("Brevo activation email failed: " + e.getMessage());
        }
    }

    //Sending email as attachment
    @Async
    public void sendEmailWithAttachment(
            String toEmail,
            String subject,
            String htmlBody,
            byte[] attachmentBytes,
            String fileName
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        String base64File = java.util.Base64.getEncoder().encodeToString(attachmentBytes);

        String body = """
    {
      "sender": {
        "name": "Money Manager",
        "email": "sauravkr81.93@gmail.com"
      },
      "to": [{
        "email": "%s"
      }],
      "subject": "%s",
      "htmlContent": "%s",
      "attachment": [{
        "content": "%s",
        "name": "%s"
      }]
    }
    """.formatted(
                toEmail,
                subject,
                htmlBody.replace("\"", "\\\""),
                base64File,
                fileName
        );

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(BREVO_URL, request, String.class);
        } catch (Exception e) {
            System.err.println("Brevo email with attachment failed: " + e.getMessage());
        }
    }

}
