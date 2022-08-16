package management.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.TokenType;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.TokenRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.UserMetadataRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.*;
import com.imemalta.api.gourmetSnApp.services.SystemConfiguration;
import com.imemalta.api.gourmetSnApp.services.TimeService;
import com.imemalta.api.gourmetSnApp.services.authentication.TokenManagement;
import com.imemalta.api.gourmetSnApp.services.authentication.UserManagement;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import com.stripe.exception.StripeException;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Main.class)
@Transactional(transactionManager="transactionManager")
public class TestTokenManagement {
    @Mock
    private TimeService timeService;
    @Mock
    private SystemConfiguration systemConfiguration;
    @Mock
    private StripeSubscriptionManagement stripeSubscriptionManagement;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMetadataRepository userMetadataRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SessionRegistry sessionRegistry;

    private TokenManagement tokenManagement;
    private UserManagement userManagement;

    @Before
    public void setUp(){
        systemConfiguration.setSendTokenEmails(false);
        tokenManagement = new TokenManagement(userRepository, timeService, systemConfiguration, tokenRepository, passwordEncoder, stripeSubscriptionManagement);
        userManagement = new UserManagement(userRepository, userMetadataRepository, passwordEncoder, sessionRegistry, tokenManagement, tokenRepository, stripeSubscriptionManagement);

        Mockito.when(systemConfiguration.getActivationTokenDuration()).thenReturn(TimeUnit.HOURS.toSeconds(24));
        Mockito.when(systemConfiguration.getFirstPasswordTokenDuration()).thenReturn(TimeUnit.HOURS.toSeconds(24));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0,0)));
    }

    @Test
    public void testActivateSignedUpUser() throws StripeException {
        signUpValidUser();

        {
            Optional<User> user = userRepository.findByUsername("someUsername1");
            TestCase.assertTrue(user.isPresent());
            TestCase.assertEquals(AccountState.PENDING_ACTIVATION, user.get().getAccountState());
        }

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.EMAIL_ACTIVATION, token.get().getTokenType());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));
        tokenManagement.activateUser(token.get().getUuid().toString());

        {
            Optional<User> user = userRepository.findByUsername("someUsername1");
            TestCase.assertTrue(user.isPresent());
            TestCase.assertEquals(AccountState.ACTIVE, user.get().getAccountState());

            Mockito.verify(stripeSubscriptionManagement, Mockito.times(1)).createCustomer(user.get());
        }

    }

    @Test
    public void testActivateAddedUser() throws StripeException {
        addValidUser();

        {
            Optional<User> user = userRepository.findByUsername("someUsername1");
            TestCase.assertTrue(user.isPresent());
            TestCase.assertEquals(AccountState.PENDING_PASSWORD, user.get().getAccountState());
        }

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.SET_FIRST_PASSWORD, token.get().getTokenType());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));
        tokenManagement.activateUserWithPassword(token.get().getUuid().toString(), "okL3ngth");

        {
            Optional<User> user = userRepository.findByUsername("someUsername1");
            TestCase.assertTrue(user.isPresent());
            TestCase.assertEquals(AccountState.ACTIVE, user.get().getAccountState());

            Mockito.verify(stripeSubscriptionManagement, Mockito.times(1)).createCustomer(user.get());
        }
    }

    @Test(expected= InvalidAccountStateException.class)
    public void testRequestActivationTokenOnAnActiveUser() {
        userRepository.save(CommonDtos.getValidUser());
        tokenManagement.sendEmailActivationToken("someUsername1");
    }

    @Test(expected= UserNotFoundException.class)
    public void testRequestActivationTokenOnAnUnknownUser() {
        tokenManagement.sendEmailActivationToken("wrongUsername");
    }

    @Test(expected= InvalidAccountStateException.class)
    public void testRequestFirstPasswordTokenTokenOnAnActiveUser() {
        userRepository.save(CommonDtos.getValidUser());
        tokenManagement.requestFirstPasswordToken("someUsername1");
    }

    @Test
    public void testSendActivationToken() {
        User user = CommonDtos.getValidUser();
        user.setAccountState(AccountState.PENDING_ACTIVATION);
        userRepository.save(user);

        tokenManagement.sendEmailActivationToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(token.get().getTokenType(), TokenType.EMAIL_ACTIVATION);
    }

    @Test(expected= UserNotFoundException.class)
    public void testRequestSetFistPasswordInvalidUsername() {
        tokenManagement.requestFirstPasswordToken("wrongUsername");
    }

    @Test
    public void testActivationWithPasswordTokenGeneration(){
        addValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
    }

    @Test(expected= InvalidAccountStateException.class)
    public void testActivateActiveUser() {
        signUpValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        user.get().setAccountState(AccountState.ACTIVE);

        userRepository.save(user.get());
        Mockito.when(timeService.getTime()).thenReturn(
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,30,0))
        );
        tokenManagement.activateUser(token.get().getUuid().toString());
    }

    @Test(expected= InvalidAccountStateException.class)
    public void testActivateActiveUserWithPassword() {
        addValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        user.get().setAccountState(AccountState.ACTIVE);
        userRepository.save(user.get());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));
        tokenManagement.activateUserWithPassword(token.get().getUuid().toString(), "okL3ngth");
    }

    @Test(expected= InvalidTokenTypeException.class)
    public void testInValidUserActivationWrongTokenType() {
        signUpValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        token.get().setTokenType(TokenType.RESET_PASSWORD);
        tokenRepository.save(token.get());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));

        tokenManagement.activateUser(token.get().getUuid().toString());
    }

    @Test(expected= TokenNotFoundException.class)
    public void testInValidUserActivationWrongUUID() {
           tokenManagement.activateUser(UUID.randomUUID().toString());
    }

    @Test(expected= ExpiredTokenException.class)
    public void testInValidUserActivationExpiredToken() {
        signUpValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 2),
                LocalTime.of(0,0,1)));

        tokenManagement.activateUser(token.get().getUuid().toString());
    }

    @Test(expected= InvalidTokenTypeException.class)
    public void testInValidUserActivationWithPasswordWrongTokenType() {
        addValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        token.get().setTokenType(TokenType.RESET_PASSWORD);
        tokenRepository.save(token.get());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));

        tokenManagement.activateUserWithPassword(token.get().getUuid().toString(), "okL3ngth");
    }

    @Test(expected= TokenNotFoundException.class)
    public void testInValidUserActivationWithPasswordWrongUUID() {
        tokenManagement.activateUserWithPassword(UUID.randomUUID().toString(), "okL3ngth");
    }

    @Test(expected= ExpiredTokenException.class)
    public void testInValidUserActivationWithPasswordExpiredToken() {
        addValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 2),
                LocalTime.of(0,0,1)));

        tokenManagement.activateUserWithPassword(token.get().getUuid().toString(), "okL3ngth");
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testValidUserActivationWithUnsecuredPassword() {
        addValidUser();

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,30,0)));

        tokenManagement.activateUserWithPassword(token.get().getUuid().toString(), "notsecure");
    }

    @Test
    public void testRequestPasswordReset() {
        userRepository.save(CommonDtos.getValidUser());

        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.RESET_PASSWORD, token.get().getTokenType());
    }

    @Test(expected= UserNotFoundException.class)
    public void testInValidRequestPasswordResetWrongUsername() {
        tokenManagement.requestPasswordResetToken("wrongUsername");
    }

    @Test
    public void testPasswordReset() {
        addUserInDatabase();

        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.RESET_PASSWORD, token.get().getTokenType());

        tokenManagement.resetPassword(token.get().getUuid().toString(), "okL3ngthReset");

        TestCase.assertTrue(passwordEncoder.matches("okL3ngthReset", token.get().getUser().getUserMetadata().getPassword()));
    }

    @Test
    public void testPasswordResetWithExpiredOldPassword() {
        addUserInDatabase();

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        user.get().getUserMetadata().setPasswordExpired(true);
        userRepository.save(user.get());

        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.RESET_PASSWORD, token.get().getTokenType());

        tokenManagement.resetPassword(token.get().getUuid().toString(),"okL3ngthReset");

        TestCase.assertTrue(passwordEncoder.matches( "okL3ngthReset", token.get().getUser().getUserMetadata().getPassword()));
        TestCase.assertFalse(token.get().getUser().getUserMetadata().isPasswordExpired());
    }

    @Test(expected= InvalidTokenTypeException.class)
    public void testPasswordResetWrongTokenType() {
        userRepository.save(CommonDtos.getValidUser());

        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        token.get().setTokenType(TokenType.EMAIL_ACTIVATION);
        tokenRepository.save(token.get());

        tokenManagement.resetPassword(token.get().getUuid().toString(),"okL3ngthReset" );
    }

    @Test(expected= TokenNotFoundException.class)
    public void testPasswordResetWrongUUID() {
        tokenManagement.resetPassword(UUID.randomUUID().toString(),"okL3ngthReset" );
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testPasswordResetNonSecurePassword() {
        userRepository.save(CommonDtos.getValidUser());

        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.RESET_PASSWORD, token.get().getTokenType());

        tokenManagement.resetPassword(token.get().getUuid().toString(),"nonsecure" );
    }

    @Test(expected= ExpiredTokenException.class)
    public void testPasswordResetExpired(){
        userRepository.save(CommonDtos.getValidUser());

        Mockito.when(systemConfiguration.getResetPasswordTokenDuration()).thenReturn(TimeUnit.HOURS.toSeconds(24));
        tokenManagement.requestPasswordResetToken("someUsername1");

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
        TestCase.assertEquals(TokenType.RESET_PASSWORD,token.get().getTokenType());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 2),
                LocalTime.of(0,30,1)));

        tokenManagement.resetPassword(token.get().getUuid().toString(),"okL3ngthReset" );
    }

    private void signUpValidUser(){
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());
    }

    private void addValidUser(){
        userManagement.addUser(CommonDtos.getValidUser());
    }

    protected void addUserInDatabase(){
        User user = CommonDtos.getValidUser();
        UserMetadata userMetadata = new UserMetadata();
        userMetadata.setUser(user);
        userMetadata.setPassword(passwordEncoder.encode(CommonDtos.defaultValidPassword()));
        user.setUserMetadata(userMetadata);

        userRepository.save(user);
        userMetadataRepository.save(userMetadata);
    }
}
