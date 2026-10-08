package com.deskflow.notifications.application.port.out;

public interface NotificationPort {
    void send(String toEmail, String subject, String body);
}
