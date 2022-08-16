package com.imemalta.api.gourmetSnApp.entities.authentication.repositories;

import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.TokenType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends CrudRepository<Token, UUID> {
    Optional<Token> findByUuid(UUID uuid);
    Optional<Token> findByUserUsername(String username);
    void deleteByUserUsername(String username);
}
