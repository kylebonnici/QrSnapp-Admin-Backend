package com.imemalta.api.gourmetSnApp.services.authentication;


import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.TokenRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.UserMetadataRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.*;
import com.imemalta.api.gourmetSnApp.exceptions.common.StripeRuntimeException;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import com.imemalta.api.gourmetSnApp.utils.SessionUtils;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserManagement {
    private final UserRepository userRepository;
    private final UserMetadataRepository userMetadataRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;
    private final TokenManagement tokenManagement;
    private final TokenRepository tokenRepository;
    private final StripeSubscriptionManagement stripeSubscriptionManagement;

    @Autowired
    public UserManagement(UserRepository userRepository, UserMetadataRepository userMetadataRepository, PasswordEncoder passwordEncoder, SessionRegistry sessionRegistry, TokenManagement tokenManagement, TokenRepository tokenRepository, StripeSubscriptionManagement stripeSubscriptionManagement) {
        this.userRepository = userRepository;
        this.userMetadataRepository = userMetadataRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
        this.tokenManagement = tokenManagement;
        this.tokenRepository = tokenRepository;
        this.stripeSubscriptionManagement = stripeSubscriptionManagement;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void signUp(User userIn, String password){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(userIn, new String[] {"accountState"}));

        if (VariantUtils.isPasswordUnsafe(password)){
            throw new PasswordNotSecureException();
        }

        checkUsernameAndEmailUniqueness(userIn);

        User newUser = new User();
        newUser.setName(userIn.getName());
        newUser.setSurname(userIn.getSurname());
        newUser.setEmail(userIn.getEmail());
        newUser.setAccountState(AccountState.PENDING_ACTIVATION);
        newUser.setUsername(userIn.getUsername());
        newUser.setValidTo(userIn.getValidTo());
        newUser.setValidFrom(userIn.getValidFrom());

        UserMetadata userMetadata = new UserMetadata();
        userMetadata.setUser(newUser);
        userMetadata.setPassword(passwordEncoder.encode(password));
        newUser.setUserMetadata(userMetadata);

        userRepository.save(newUser);
        userMetadataRepository.save(userMetadata);

        tokenManagement.sendEmailActivationToken(userIn.getUsername());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public User addUser(User userDto){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(userDto, new String[] {"password", "accountState"}));

        checkUsernameAndEmailUniqueness(userDto);


        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setSurname(userDto.getSurname());
        newUser.setEmail(userDto.getEmail());
        newUser.setAccountState(AccountState.PENDING_PASSWORD);
        newUser.setUsername(userDto.getUsername());
        newUser.setValidTo(userDto.getValidTo());
        newUser.setValidFrom(userDto.getValidFrom());

        UserMetadata userMetadata = new UserMetadata();
        userMetadata.setUser(newUser);
        newUser.setUserMetadata(userMetadata);

        userRepository.save(newUser);
        userMetadataRepository.save(userMetadata);

        tokenManagement.requestFirstPasswordToken(userDto.getUsername());

        return newUser;
    }

    private void checkUsernameAndEmailUniqueness(User userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyInUseException();
        }
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        return user.get();
    }

    @Transactional(readOnly = true)
    public Iterable<User> getUsers(){
        return userRepository.findAll();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (username.equals(VariantUtils.currentSessionUsername())){
            throw new DeleteOwnUserException();
        }

        SessionUtils.expireUserSessions(user.get().getUsername(), sessionRegistry);

        Optional<Token> token = tokenRepository.findByUserUsername(user.get().getUsername());
        token.ifPresent(tokenRepository::delete);

        userRepository.delete(user.get());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public User updateUser(String username, User userIn){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(userIn, new String[] {"password"}));

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        boolean accountStateChanged = userIn.getAccountState().compareTo(user.get().getAccountState()) != 0;

        if ( username.equals(VariantUtils.currentSessionUsername())) {
            if (    (userIn.getValidFrom() != user.get().getValidFrom() ||
                    (userIn.getValidFrom() != null &&  user.get().getValidFrom() != null && userIn.getValidFrom().compareTo(user.get().getValidFrom()) != 0))
                    ||
                    (userIn.getValidTo() != user.get().getValidTo() ||
                            (userIn.getValidTo() != null &&  user.get().getValidTo() != null && userIn.getValidTo().compareTo(user.get().getValidTo()) != 0))){
                throw new ChangeOwnAccountActiveDatesException();
            }

            if (accountStateChanged) {
                throw new ChangeOwnAccountStateException();
            }
        }

        if (accountStateChanged &&
                (userIn.getAccountState() == AccountState.PENDING_PASSWORD || userIn.getAccountState() == AccountState.PENDING_ACTIVATION)) {
            throw new InvalidAccountStateChangeException();
        }

        if (!userIn.getUsername().equals(username)) {
            Optional<User> newUser = userRepository.findByUsername(userIn.getUsername());

            if (newUser.isPresent()) {
                throw new UsernameAlreadyInUseException();
            }
        }

        userIn.setEmail(userIn.getEmail().toLowerCase());

        if (!user.get().getEmail().equals(userIn.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(userIn.getEmail());
            if (userByEmail.isPresent()) {
                throw new EmailAlreadyRegisteredException();
            }
        }

        user.get().setName(userIn.getName());
        user.get().setSurname(userIn.getSurname());
        user.get().setValidFrom(userIn.getValidFrom());
        user.get().setValidTo(userIn.getValidTo());
        user.get().setUsername(userIn.getUsername());
        user.get().setEmail(userIn.getEmail().toLowerCase());
        user.get().setAccountState(userIn.getAccountState());

        userRepository.save(user.get());
        try {
            stripeSubscriptionManagement.updateCustomer(user.get());
        } catch (StripeException stripeException) {
            throw new StripeRuntimeException(stripeException);
        }

        return user.get();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void changePassword(String username, String currentPasswordOfUserChangingThePassword, String newPassword) {
        if (VariantUtils.isPasswordUnsafe(newPassword)){
            throw new PasswordNotSecureException();
        }

        Optional<User> userToChange = userRepository.findByUsername(username);

        if (userToChange.isEmpty()) {
            throw new UserNotFoundException();
        }

        String currentUserName = VariantUtils.currentSessionUsername();
        Optional<User> currentUser = userRepository.findByUsername(currentUserName);
        assert(currentUser.isPresent());

        if (!passwordEncoder.matches(currentPasswordOfUserChangingThePassword, currentUser.get().getUserMetadata().getPassword())){
            throw new IncorrectPasswordException();
        }

        userToChange.get().getUserMetadata().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userToChange.get());
    }

    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username){
        Optional<User> userByUserNameOptional = userRepository.findByUsername(username);
        return userByUserNameOptional.isPresent();
    }
}
