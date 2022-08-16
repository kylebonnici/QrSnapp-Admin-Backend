package com.imemalta.api.gourmetSnApp.entities.listeners;

import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.TokenRepository;
import com.imemalta.api.gourmetSnApp.services.BeanUtil;

import javax.persistence.PrePersist;

public class TokenCleanerListener {
    @PrePersist
    public void prePersist(Token token) {
        TokenRepository tokenRepository = BeanUtil.getBean(TokenRepository.class);
        tokenRepository.deleteByUserUsername(token.getUser().getUsername());
    }
}
