package com.imemalta.api.gourmetSnApp.services;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimeService implements Serializable{
    public LocalDateTime getTime() {
        return LocalDateTime.now();
    }

    public LocalDateTime getTime(String zoneId) {
        return LocalDateTime.now(ZoneId.of(zoneId));
    }
}
