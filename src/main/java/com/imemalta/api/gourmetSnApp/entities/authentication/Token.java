package com.imemalta.api.gourmetSnApp.entities.authentication;

import com.imemalta.api.gourmetSnApp.entities.authentication.enums.TokenType;
import com.imemalta.api.gourmetSnApp.entities.listeners.TokenCleanerListener;
import com.imemalta.api.gourmetSnApp.entities.listeners.TokenEmailListener;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Tokens")
@EntityListeners({TokenEmailListener.class, TokenCleanerListener.class})
public class Token {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "uuid", columnDefinition = "BINARY(16)", unique = true)
    private UUID uuid;
    private LocalDateTime timeStamp;
    private long validDuration; // in Seconds

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name="userID")
    private User user;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(long validDuration) {
        this.validDuration = validDuration;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
