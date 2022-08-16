package com.imemalta.api.gourmetSnApp.entities.listeners;

import com.imemalta.api.gourmetSnApp.components.EmailService;
import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.services.BeanUtil;
import com.imemalta.api.gourmetSnApp.services.SystemConfiguration;
import org.springframework.mail.SimpleMailMessage;

import javax.persistence.PostPersist;
import java.util.concurrent.TimeUnit;

public class TokenEmailListener{
    @PostPersist
    public void postPersist(Token token) {
        new Thread(() -> process(token)).start();
    }

    private void process(Token token) {
        SystemConfiguration systemConfiguration = BeanUtil.getBean(SystemConfiguration.class);

        if (systemConfiguration.shouldSendTokenEmails()) {
            EmailService emailService = BeanUtil.getBean(EmailService.class);

            switch (token.getTokenType()) {
                case EMAIL_ACTIVATION:
                    sendAccountActivationEmail(emailService, systemConfiguration, token);
                    break;
                case RESET_PASSWORD:
                    sendResetPasswordEmail(emailService, systemConfiguration, token);
                    break;
                case SET_FIRST_PASSWORD:
                    sendInviteEmail(emailService, systemConfiguration, token);
                    break;
            }
        }
    }

    private void sendInviteEmail(EmailService emailService, SystemConfiguration systemConfiguration, Token token) {
        SimpleMailMessage message = inviteEmailTemplate();

        String url = systemConfiguration.getAppURL() + "/invite?token=" + token.getUuid().toString() + "&username=" + token.getUser().getUsername();

        String text = String.format(message.getText(), token.getUser().getName(), token.getUser().getUsername(), url, TimeUnit.MILLISECONDS.toHours(systemConfiguration.getFirstPasswordTokenDuration()));
        emailService.sendSimpleMessage(token.getUser().getEmail(), "Invite to join " + systemConfiguration.getApplicationName(), text);
    }

    private SimpleMailMessage inviteEmailTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Hi %s,\n\n" +
                        "An account has been created for you. Your username is '%s'. Please use the link below to accept this invitation and set your password." +
                        "\n%s\n This link will remain valid for the next %d hours. If this link expires you have to request a new one from the system administrator\n\n QRSnApp" );
        return message;
    }

    private void sendResetPasswordEmail(EmailService emailService, SystemConfiguration systemConfiguration, Token token) {
        SimpleMailMessage message = resetPasswordEmailTemplate();

        String url = systemConfiguration.getAppURL() + "/reset?token=" + token.getUuid().toString() + "&username=" + token.getUser().getUsername();

        String text = String.format(message.getText(), token.getUser().getName(), url, TimeUnit.MILLISECONDS.toHours(systemConfiguration.getResetPasswordTokenDuration()));
        emailService.sendSimpleMessage(token.getUser().getEmail(), "Password Reset", text);
    }

    private SimpleMailMessage resetPasswordEmailTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Hi %s,\n\n" +
                        "We have received your request to reset your password. Please follow the hyperlink provided to reset your password." +
                        "\n%s\n This link will remain valid for the next %d hours.\n\n QRSnApp" );
        return message;
    }

    private void sendAccountActivationEmail(EmailService emailService, SystemConfiguration systemConfiguration, Token token) {
        SimpleMailMessage message = accountActivationEmailTemplate();

        String url = systemConfiguration.getAppURL() + "/activate?token=" + token.getUuid().toString() + "&username=" + token.getUser().getUsername();

        String text = String.format( message.getText(), token.getUser().getName(), url,  TimeUnit.MILLISECONDS.toHours(systemConfiguration.getActivationTokenDuration()));
        emailService.sendSimpleMessage(token.getUser().getEmail(), "Account Activation", text);
    }

    private SimpleMailMessage accountActivationEmailTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText(
                "Hi %s,\n\n" +
                        "Thanks for signing up. Please use the hyperlink provided to verify and activate your account. " +
                        "\n%s\n This link will remain valid for the next %d hours.\n\n QRSnApp" );
        return message;
    }
}
