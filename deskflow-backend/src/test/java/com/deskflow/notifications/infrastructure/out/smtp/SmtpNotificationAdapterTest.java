package com.deskflow.notifications.infrastructure.out.smtp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpNotificationAdapterTest {

    @Mock private JavaMailSender mailSender;
    @Captor private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void sends_email_with_correct_fields() {
        SmtpNotificationAdapter adapter = new SmtpNotificationAdapter(mailSender, false);

        adapter.send("user@test.com", "Subject", "Body text");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage msg = messageCaptor.getValue();
        assertThat(msg.getTo()).containsExactly("user@test.com");
        assertThat(msg.getSubject()).isEqualTo("Subject");
        assertThat(msg.getText()).isEqualTo("Body text");
        assertThat(msg.getFrom()).isEqualTo("noreply@deskflow.local");
    }

    @Test
    void mail_exception_is_caught_and_logged() {
        SmtpNotificationAdapter adapter = new SmtpNotificationAdapter(mailSender, false);
        doThrow(new MailException("SMTP error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        adapter.send("user@test.com", "Subject", "Body");
    }

    @Test
    void sends_to_different_recipients() {
        SmtpNotificationAdapter adapter = new SmtpNotificationAdapter(mailSender, false);

        adapter.send("alice@test.com", "A", "msg1");
        adapter.send("bob@test.com", "B", "msg2");

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void does_not_send_when_smtp_is_disabled() {
        SmtpNotificationAdapter adapter = new SmtpNotificationAdapter(mailSender, true);

        adapter.send("user@test.com", "Subject", "Body text");

        verifyNoInteractions(mailSender);
    }
}
