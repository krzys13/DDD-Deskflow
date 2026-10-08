package com.deskflow.notifications.infrastructure.out.smtp;

import com.deskflow.notifications.application.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificationAdapter.class);

    private final JavaMailSender mailSender;
    private final boolean smtpOff;

    public SmtpNotificationAdapter(JavaMailSender mailSender,
                                   @Value("${app.smtp.off:false}") boolean smtpOff) {
        this.mailSender = mailSender;
        this.smtpOff = smtpOff;
    }

    @Override
    public void send(String toEmail, String subject, String body) {
        if (smtpOff) {
            log.debug("SMTP disabled; skipping email to {}", toEmail);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@deskflow.local");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
