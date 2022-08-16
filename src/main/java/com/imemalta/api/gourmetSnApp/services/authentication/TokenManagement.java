package com.imemalta.api.gourmetSnApp.services.authentication;

import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.TokenType;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.TokenRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.*;
import com.imemalta.api.gourmetSnApp.exceptions.common.StripeRuntimeException;
import com.imemalta.api.gourmetSnApp.services.SystemConfiguration;
import com.imemalta.api.gourmetSnApp.services.TimeService;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TokenManagement {
    private final UserRepository userRepository;
    private final TimeService timeService;
    private final SystemConfiguration systemConfiguration;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final StripeSubscriptionManagement stripeSubscriptionManagement;

    @Autowired
    public TokenManagement(UserRepository userRepository, TimeService timeService, SystemConfiguration systemConfiguration, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, StripeSubscriptionManagement stripeSubscriptionManagement) {
        this.userRepository = userRepository;
        this.timeService = timeService;
        this.systemConfiguration = systemConfiguration;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.stripeSubscriptionManagement = stripeSubscriptionManagement;
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    public void requestFirstPasswordToken(String username){
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (user.get().getAccountState() != AccountState.PENDING_PASSWORD) {
            throw new InvalidAccountStateException();
        }

        Token token = new Token();
        token.setTimeStamp(timeService.getTime());
        token.setTokenType(TokenType.SET_FIRST_PASSWORD);
        token.setValidDuration(systemConfiguration.getFirstPasswordTokenDuration());
        token.setUser(user.get());

        tokenRepository.save(token);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void requestPasswordResetToken(String username){
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        Token token = new Token();
        token.setTimeStamp(timeService.getTime());
        token.setTokenType(TokenType.RESET_PASSWORD);
        token.setValidDuration(systemConfiguration.getResetPasswordTokenDuration());
        token.setUser(user.get());

        tokenRepository.save(token);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void sendEmailActivationToken(String username){
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (user.get().getAccountState() != AccountState.PENDING_ACTIVATION) {
            throw new InvalidAccountStateException();
        }

        Token token = new Token();
        token.setTimeStamp(timeService.getTime());
        token.setTokenType(TokenType.EMAIL_ACTIVATION);
        token.setValidDuration(systemConfiguration.getActivationTokenDuration());
        token.setUser(user.get());

        tokenRepository.save(token);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void activateUser(String tokenStr){
        Optional<Token> token = tokenRepository.findByUuid(UUID.fromString(tokenStr));

        validateToken(token, TokenType.EMAIL_ACTIVATION);
        assert(token.isPresent());

        if (token.get().getUser().getAccountState() != AccountState.PENDING_ACTIVATION) {
            throw new InvalidAccountStateException();
        }

        User user = token.get().getUser();
        user.setAccountState(AccountState.ACTIVE);
        userRepository.save(user);
        tokenRepository.delete(token.get());
        try {
            stripeSubscriptionManagement.createCustomer(user);
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void activateUserWithPassword(String tokenStr, String password) {
        Optional<Token> token = tokenRepository.findByUuid(UUID.fromString(tokenStr));

        validateToken(token, TokenType.SET_FIRST_PASSWORD);
        assert(token.isPresent());

        if (VariantUtils.isPasswordUnsafe(password)){
            throw new PasswordNotSecureException();
        }

        if (token.get().getUser().getAccountState() != AccountState.PENDING_PASSWORD) {
            throw new InvalidAccountStateException();
        }

        User user = token.get().getUser();
        user.setAccountState(AccountState.ACTIVE);
        user.getUserMetadata().setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(token.get());

        try {
            stripeSubscriptionManagement.createCustomer(user);
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void resetPassword( String tokenStr, String password){
        Optional<Token> token = tokenRepository.findByUuid(UUID.fromString(tokenStr));

        validateToken(token, TokenType.RESET_PASSWORD);
        assert(token.isPresent());

        if (VariantUtils.isPasswordUnsafe(password)){
            throw new PasswordNotSecureException();
        }

        token.get().getUser().getUserMetadata().setPassword(passwordEncoder.encode(password));

        if (token.get().getUser().getUserMetadata().isPasswordExpired()) {
            token.get().getUser().getUserMetadata().setPasswordExpired(false);
        }

        userRepository.save(token.get().getUser());
        tokenRepository.delete(token.get());
    }

    private void validateToken(final Optional<Token> token, final TokenType tokenType){
        if (token.isEmpty()) {
            throw new TokenNotFoundException();
        }

        if (token.get().getTokenType() != tokenType) {
            throw new InvalidTokenTypeException();
        }

        if (VariantUtils.hasDurationElapsed(timeService.getTime(), token.get().getValidDuration(), token.get().getTimeStamp())){
            tokenRepository.delete(token.get());
            throw new ExpiredTokenException();
        }
    }
}
