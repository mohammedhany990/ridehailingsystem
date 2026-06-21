package com.ridehailing.authservice.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendHtmlEmail(EmailDetails details) {

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(details.getTo());
            helper.setSubject(details.getSubject());
            helper.setText(details.getMessage(), true);

            mailSender.send(mimeMessage);

            log.info("Email sent to {}", details.getTo());

        } catch (MessagingException | MailException ex) {

            log.error("Failed to send email to {}", details.getTo(), ex);

            throw new RuntimeException("Failed to send email");
        }
    }
}