package com.imemalta.api.gourmetSnApp.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SystemConfiguration {
    public static final String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
    public static final String emailRegex = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$";

    private final long activationTokenDuration = TimeUnit.HOURS.toMillis(24);
    private final long firstPasswordTokenDuration = TimeUnit.HOURS.toMillis(24);
    private final long resetPasswordTokenDuration = TimeUnit.HOURS.toMillis(24);

    @Value("${my.appUrl}")
    private String appURL = "http://127.0.0.1:4200";

    @Value("${my.sendTokenEmails}")
    private boolean sendTokenEmails = true;

    public long getActivationTokenDuration() {
        return activationTokenDuration;
    }

    public long getFirstPasswordTokenDuration() {
        return firstPasswordTokenDuration;
    }

    public long getResetPasswordTokenDuration() {
        return resetPasswordTokenDuration;
    }

    public String getAppURL() {
        return appURL;
    }

    public String getApplicationName() {
        return "Gourmet SnApp";
    }

    public boolean shouldSendTokenEmails() {
        return sendTokenEmails;
    }

    public void setSendTokenEmails(boolean sendTokenEmails) {
        this.sendTokenEmails = sendTokenEmails;
    }
}
