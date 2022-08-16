package com.imemalta.api.gourmetSnApp.components;

public interface EmailService {
    void sendSimpleMessage(
            String to, String subject, String text);
}
