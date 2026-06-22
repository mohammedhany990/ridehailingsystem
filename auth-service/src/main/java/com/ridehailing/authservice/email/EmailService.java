package com.ridehailing.authservice.email;


import com.ridehailing.authservice.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Async("emailTaskExecutor")
    public void sendHtmlEmail(EmailDetails emailDetails) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");

            helper.setFrom(sender);
            helper.setTo(emailDetails.getTo());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessage(), true);

            mailSender.send(mimeMessage);

            log.info("HTML email sent to {}", emailDetails.getTo());

        } catch (MessagingException ex) {
            log.error("Failed to send HTML email to {}", emailDetails.getTo(), ex);
            throw new EmailSendingException("Failed to send HTML email");
        }
    }
}