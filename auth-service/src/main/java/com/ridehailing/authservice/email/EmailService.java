package com.ridehailing.authservice.email;

import com.ridehailing.authservice.exception.EmailSendingException;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @PostConstruct
    public void validateConfig() {
        Assert.hasText(sender, "spring.mail.username must not be empty");
    }

    @Async("emailTaskExecutor")
    @Retryable(
            value = { MailException.class, MessagingException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendHtmlEmail(EmailDetails emailDetails) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8"
            );

            helper.setFrom(sender);
            helper.setTo(emailDetails.getTo());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessage(), true);

            mailSender.send(mimeMessage);


            log.info("ENTERED sendHtmlEmail for {}", emailDetails.getTo());

        } catch (MessagingException ex) {
            log.error("Messaging error while sending email to {}", emailDetails.getTo(), ex);
            throw new EmailSendingException("Failed to build email message");
        } catch (MailException ex) {
            log.error("Mail server error while sending email to {}", emailDetails.getTo(), ex);
            throw ex;
        }
    }
}