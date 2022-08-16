package management.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.authentication.Token;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.TokenRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;

import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.UserMetadataRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.*;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Main.class)
@Transactional(transactionManager="transactionManager")
public class TestUserManagement {

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

    @Mock
    private SystemConfiguration systemConfiguration;
    @Mock
    private TimeService timeService;
    @Mock
    private StripeSubscriptionManagement stripeSubscriptionManagement;

    private UserManagement userManagement;

    @Before
    public void clearTables(){
        systemConfiguration.setSendTokenEmails(false);
        TokenManagement tokenManagement = new TokenManagement(userRepository, timeService, systemConfiguration, tokenRepository, passwordEncoder, stripeSubscriptionManagement);
        userManagement = new UserManagement(userRepository, userMetadataRepository, passwordEncoder,sessionRegistry, tokenManagement, tokenRepository, stripeSubscriptionManagement);
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpNoEmail_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setEmail(null);
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpNoName_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setName(null);
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpNoSurname_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setSurname(null);
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpNoUsername_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setUsername(null);
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testSignUpNoPassword_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        userManagement.signUp(user, null);
    }
    @Test // not exception
    public void testSignUp_ConstraintViolation_Pass() {
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserNoEmail_ConstraintViolation() {
        User user = CommonDtos.getValidUser();

        user.setEmail(null);
        userManagement.addUser(user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserNoName_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setName(null);
        userManagement.addUser(user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserNoSurname_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setSurname(null);
        userManagement.addUser(user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserNoUsername_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        user.setUsername(null);
        userManagement.addUser(user);
    }
    @Test // no exception
    public void testAddUser_ConstraintViolation_valid() {
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testUpdateUserNoEmail_ConstraintViolation() {
        userRepository.save(CommonDtos.getValidUser());

        User user = CommonDtos.getValidUser();
        user.setEmail(null);
        userManagement.updateUser("someUsername1", user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testUpdateUserNoName_ConstraintViolation() {
        userRepository.save(CommonDtos.getValidUser());

        User user = CommonDtos.getValidUser();
        user.setName(null);
        userManagement.updateUser("someUsername1", user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testUpdateUserNoSurname_ConstraintViolation(){
        userRepository.save(CommonDtos.getValidUser());

        User user = CommonDtos.getValidUser();
        user.setSurname(null);
        userManagement.updateUser("someUsername1", user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testUpdateUserNoUsername_ConstraintViolation(){
        userRepository.save(CommonDtos.getValidUser());

        User user = CommonDtos.getValidUser();
        user.setUsername(null);
        userManagement.updateUser("someUsername1", user);
    }

    @Test // No exception
    @WithMockUser(username="someUsername1")
    public void testUpdateUser_ConstraintViolation_valid(){
        userRepository.save(CommonDtos.getValidUser());

        userManagement.updateUser("someUsername1", CommonDtos.getValidUser());
    }

    @Test(expected = EmailAlreadyRegisteredException.class)
    @WithMockUser(username="someAdminUser")
    public void testUpdateUserDuplicateEmail(){
        addTwoUsersInDatabase();

        User updatedUser2 = CommonDtos.getValidUser();
        updatedUser2.setUsername("someUsername2");
        updatedUser2.setEmail("user1Email@email.com".toLowerCase());

        userManagement.updateUser("someUsername2", updatedUser2);
    }


    @Test(expected = InvalidAccountStateChangeException.class)
    @WithMockUser(username="someAdminUser")
    public void testChangeStateToNotActive(){
        User user = addUserInDatabase();
        User updatedUser1 = new User();

        updatedUser1.setEmail("someDifferentEmail@email.com");
        updatedUser1.setName("someName");
        updatedUser1.setSurname("someSurname");
        updatedUser1.setUsername("someDifferentUsername");
        updatedUser1.setAccountState(AccountState.ACTIVE);

        userManagement.updateUser(user.getUsername(), updatedUser1);

        updatedUser1.setAccountState(AccountState.PENDING_PASSWORD);
        userManagement.updateUser(updatedUser1.getUsername(), updatedUser1);
    }

    @Test(expected = InvalidAccountStateChangeException.class)
    @WithMockUser(username="someAdminUSer")
    public void testChangeStateToPendingPassword(){
        User user = CommonDtos.getValidUser();
        userManagement.addUser(user);
        User updatedUser1 = new User();

        updatedUser1.setEmail("someDifferentEmail@email.com");
        updatedUser1.setName("someName");
        updatedUser1.setSurname("someSurname");
        updatedUser1.setUsername("someDifferentUsername");
        updatedUser1.setAccountState(AccountState.ACTIVE);

        userManagement.updateUser(user.getUsername(), updatedUser1);

        updatedUser1.setAccountState(AccountState.PENDING_PASSWORD);
        userManagement.updateUser(updatedUser1.getUsername(), updatedUser1);
    }

    @Test(expected = UsernameAlreadyInUseException.class)
    @WithMockUser(username="someAdminUser")
    public void testUpdateUserDuplicateUsername(){
        addTwoUsersInDatabase();

        User updatedUser2 = CommonDtos.getValidUser();
        updatedUser2.setUsername("someUsername1");

        userManagement.updateUser("someUsername2", updatedUser2);
    }

    @Test // no exception
    @WithMockUser(username="someAdminUser")
    public void testUpdateUserChangeValidityDates() throws StripeException {
        User user = addUserInDatabase();
        User updatedUser = CommonDtos.getValidUser();

        updatedUser.setValidFrom(LocalDateTime.now());
        updatedUser.setValidTo(LocalDateTime.now());

        userManagement.updateUser(user.getUsername(), updatedUser);
        Mockito.verify(stripeSubscriptionManagement, Mockito.times(1)).updateCustomer(user);
    }

    @Test(expected = ChangeOwnAccountActiveDatesException.class)
    @WithMockUser(username="someUsername1")
    public void testUpdateUserChangeFromValidityDatesSameUser(){
        addUserInDatabase();

        User updatedUser = CommonDtos.getValidUser();
        updatedUser.setValidFrom(LocalDateTime.now());

        userManagement.updateUser("someUsername1", updatedUser);
    }

    @Test(expected = ChangeOwnAccountActiveDatesException.class)
    @WithMockUser(username="someUsername1")
    public void testUpdateUserChangeToValidityDatesSameUser(){
        addUserInDatabase();

        User updatedUser = CommonDtos.getValidUser();
        updatedUser.setValidTo(LocalDateTime.now());

        userManagement.updateUser("someUsername1", updatedUser);
    }

    @Test
    @WithMockUser(username="someAdminUser")
    public void testDeleteUser(){
        userRepository.save(CommonDtos.getValidUser());

        userManagement.delete("someUsername1");

        TestCase.assertFalse(tokenRepository.findByUserUsername("someUsername1").isPresent());
        TestCase.assertFalse(userRepository.findByUsername("someUsername1").isPresent());
    }

    @Test(expected = DeleteOwnUserException.class)
    @WithMockUser(username="someUsername1")
    public void testDeleteOwnUser(){
        userRepository.save(CommonDtos.getValidUser());
        userManagement.delete("someUsername1");
    }

    @Test(expected = UserNotFoundException.class)
    public void testDeleteNonExistingUser(){
        userManagement.delete("someWrongUsername");
    }

    @Test
    public void testGetUser(){
        User user = addUserInDatabase();
        TestCase.assertEquals(user.getUsername(), userManagement.getUser(user.getUsername()).getUsername());
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetNonExistingUser(){
        userManagement.getUser("someWrongUsername");
    }

    @Test
    public void testGetUsers(){
        User user1 = CommonDtos.getValidUser();
        user1.setUsername("Username1");
        userManagement.addUser(user1);

        User user2 = CommonDtos.getValidUser();
        user2.setEmail(user2.getEmail() + "N");
        user2.setUsername("Username2");
        userManagement.addUser(user2);

        Iterable<User> userIterable = userManagement.getUsers();
        List<User> users = new ArrayList<>();
        userIterable.forEach(users::add);
        TestCase.assertEquals(2, users.size());
    }

    @Test
    public void testIsUsernameTaken_True(){
        userRepository.save(CommonDtos.getValidUser());
        TestCase.assertTrue(userManagement.isUsernameTaken("someUsername1"));
    }

    @Test
    public void testIsUsernameTaken_False(){
        TestCase.assertFalse(userManagement.isUsernameTaken("someUsername1"));
    }

    @Test(expected = ChangeOwnAccountStateException.class)
    @WithMockUser(username="someUsername1")
    public void testUpdateUserChangeAccountStateSameUser(){
        User user = addUserInDatabase();
        User updatedUser = CommonDtos.getValidUser();

        updatedUser.setAccountState(AccountState.LOCKED);

        userManagement.updateUser(user.getUsername(), updatedUser);
    }

    @Test(expected= UserNotFoundException.class)
    public void testUpdateUserWrongUsername(){
        userManagement.updateUser("wronguser", CommonDtos.getValidUser());
    }

    @Test
    public void testSignUpValidUser(){
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());
    }

    @Test(expected = EmailAlreadyRegisteredException.class)
    public void testSignUpValidUserTwiceDuplicateEmail(){
        userRepository.save(CommonDtos.getValidUser());
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());
    }

    @Test(expected = UsernameAlreadyInUseException.class)
    public void testSignUpValidUserTwiceDuplicateUserName(){
        userRepository.save(CommonDtos.getValidUser());

        User user = CommonDtos.getValidUser();
        user.setEmail(user.getEmail() + "N"); // needed to avoid conflict with email.

        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test // No exception
    public void testAddUserValidUser(){
        userManagement.addUser(CommonDtos.getValidUser());
    }

    @Test(expected = EmailAlreadyRegisteredException.class)
    public void testAddUserValidUserTwiceSameEmail(){
        User user = CommonDtos.getValidUser();

        userManagement.addUser(user);
        userManagement.addUser(user);
    }

    @Test(expected = UsernameAlreadyInUseException.class)
    public void testAddUserValidUserTwiceSameUser(){
        User user = CommonDtos.getValidUser();

        userManagement.addUser(user);

        user.setEmail(user.getEmail() + "N");
        userManagement.addUser(user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpInvalidEmail_ConstraintViolation_case1() {
        User user = CommonDtos.getValidUser();

        user.setEmail("someEmailemail.com");
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testSignUpInvalidEmail_ConstraintViolation_case2(){
        User user = CommonDtos.getValidUser();

        user.setEmail("someEmail@emailcom");
        userManagement.signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserInvalidEmail_ConstraintViolation_case1() {
        User user = CommonDtos.getValidUser();

        user.setEmail("someEmailemail.com");
        userManagement.addUser(user);
    }

    @Test(expected= ConstraintViolationException.class)
    public void testAddUserInvalidEmail_ConstraintViolation_case2(){
        User user = CommonDtos.getValidUser();

        user.setEmail("someEmail@emailcom");
        userManagement.addUser(user);
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testSignUpNonSecurePassword_ConstraintViolation_case1() {
        User user = CommonDtos.getValidUser();

        userManagement.signUp(user, "short");
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testSignUpNonSecurePassword_ConstraintViolation_case2() {
        User user = CommonDtos.getValidUser();

        userManagement.signUp(user, "oklength");
    }

    @Test(expected= PasswordNotSecureException.class)
    public void testSignUpNonSecurePassword_ConstraintViolation_case3() {
        User user = CommonDtos.getValidUser();

        userManagement.signUp(user, "okLength");
    }

    @Test // no exception
    public void testSignUpSecurePassword_ConstraintViolation_valid() {
        User user = CommonDtos.getValidUser();

        userManagement.signUp(user, "okL3ngth");
    }

    @Test
    public void testSignUpUserPersistence(){
        User newUser = CommonDtos.getValidUser();

        userManagement.signUp(newUser, CommonDtos.defaultValidPassword());

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(AccountState.PENDING_ACTIVATION, user.get().getAccountState() );
    }

    @Test
    public void testAddUserUserPersistence(){
        User newUser = CommonDtos.getValidUser();

        userManagement.addUser(newUser);

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(AccountState.PENDING_PASSWORD, user.get().getAccountState() );
    }

    @Test
    public void testActivationTokenGeneration(){
        userManagement.signUp(CommonDtos.getValidUser(), CommonDtos.defaultValidPassword());

        Optional<Token> token = tokenRepository.findByUserUsername("someUsername1");
        TestCase.assertTrue(token.isPresent());
    }

    @Test
    @WithMockUser(username="someUsername1")
    public void testChangeUsername(){
        userRepository.save(CommonDtos.getValidUser());

        User updatedUser = CommonDtos.getValidUser();
        updatedUser.setUsername("someUsername1Updated");

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());

        userManagement.updateUser("someUsername1", updatedUser);

        user = userRepository.findByUsername("someUsername1");
        TestCase.assertFalse(user.isPresent());

        user = userRepository.findByUsername("someUsername1Updated");
        TestCase.assertTrue(user.isPresent());
    }

    @Test(expected= UserNotFoundException.class)
    public void testChangeUsernameInvalidUsername_case1(){
        userManagement.updateUser("someUser", CommonDtos.getValidUser());
    }

    @Test(expected= UserNotFoundException.class)
    public void testChangeUsernameInvalidUsername_case2(){
        userManagement.updateUser(null, CommonDtos.getValidUser());
    }

    @Test(expected = UsernameAlreadyInUseException.class)
    @WithMockUser(username="someAdminUser")
    public void testChangeUsernameInvalidExistingUsername(){
        addTwoUsersInDatabase();

        User updatedUser2 = CommonDtos.getValidUser();
        updatedUser2.setUsername("someUsername1");
        userManagement.updateUser("someUsername2", updatedUser2);
    }

    @Test
    @WithMockUser(username="someUsername1")
    public void testChangeOwnPassword(){
        addUserInDatabase();

        userManagement.changePassword("someUsername1", "okL3ngth","okL3ngthNew");

        Optional<User> user = userRepository.findByUsername("someUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertTrue(passwordEncoder.matches("okL3ngthNew", user.get().getUserMetadata().getPassword()));
    }

    @Test
    @WithMockUser(username="someUsername1")
    public void testChangeSomeOtherUserPassword(){
        addTwoUsersInDatabase();

        userManagement.changePassword("someUsername2", "okL3ngthUser1","okL3ngthUser2New");

        Optional<User> user = userRepository.findByUsername("someUsername2");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertTrue(passwordEncoder.matches("okL3ngthUser2New", user.get().getUserMetadata().getPassword()));
    }

    @Test(expected = IncorrectPasswordException.class)
    @WithMockUser(username="someUsername1")
    public void testChangePasswordWrongPassword(){
        User newUser = addUserInDatabase();

        Optional<User> user = userRepository.findByUsername(newUser.getUsername());
        TestCase.assertTrue(user.isPresent());

        userManagement.changePassword("someUsername1",
                newUser.getUserMetadata().getPassword() + "Wrong",
                newUser.getUserMetadata().getPassword() + "New");
    }

    @Test(expected = PasswordNotSecureException.class)
    @WithMockUser(username="someUsername1")
    public void testChangePasswordNotSecure(){
        User newUser = addUserInDatabase();

        Optional<User> user = userRepository.findByUsername(newUser.getUsername());
        TestCase.assertTrue(user.isPresent());

        userManagement.changePassword("someUsername1", newUser.getUserMetadata().getPassword(),"notSecure");
    }

    @Test(expected= UserNotFoundException.class)
    public void testChangePasswordInvalidUsername(){
        userManagement.changePassword("wrongUsername",  "OldSecu3Password","NewSecu3Password");
    }

    protected User addUserInDatabase(){
        User user = CommonDtos.getValidUser();
        UserMetadata userMetadata = new UserMetadata();
        userMetadata.setUser(user);
        userMetadata.setPassword(passwordEncoder.encode(CommonDtos.defaultValidPassword()));
        user.setUserMetadata(userMetadata);

        userRepository.save(user);
        userMetadataRepository.save(userMetadata);

        return user;
    }

    private void addTwoUsersInDatabase() {
        User user1 = CommonDtos.getValidUser();
        user1.setUsername("someUsername1");
        user1.setEmail("user1Email@email.com".toLowerCase());
        UserMetadata userMetadata1 = new UserMetadata();
        userMetadata1.setUser(user1);
        userMetadata1.setPassword(passwordEncoder.encode("okL3ngthUser1"));
        user1.setUserMetadata(userMetadata1);

        User user2 = CommonDtos.getValidUser();
        user2.setUsername("someUsername2");
        user2.setEmail("user2Email@email.com".toLowerCase());
        UserMetadata userMetadata2 = new UserMetadata();
        userMetadata2.setUser(user2);
        userMetadata2.setPassword(passwordEncoder.encode("okL3ngthUser2"));
        user2.setUserMetadata(userMetadata2);

        userRepository.save(user1);
        userMetadataRepository.save(userMetadata1);

        userRepository.save(user2);
        userMetadataRepository.save(userMetadata2);
    }
}
