package com.imemalta.api.gourmetSnApp;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan( basePackages = {"com.imemalta.api.gourmetSnApp.entities"} )
public class Main {
    @Value("${stripe.secret_key}")
    private String secretKey;

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

    @PostConstruct
    public void init(){
        // Setting Spring Boot SetTimeZone
        Stripe.apiKey = secretKey;

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
