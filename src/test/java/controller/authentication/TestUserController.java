package controller.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.authentication.UserController;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.services.authentication.UserManagement;
import helpers.CommonDtos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class TestUserController {
    @SuppressWarnings("unused")
    @MockBean
    private UserManagement userManagement;

    @Mock
    private HttpSession mockHttpSession;

    @Autowired
    private UserController userController;


    @Test // No exceptions
    public void testOpenAccessToLogin(){
        userController.login();
    }

    @Test
    public void testOpenAccessToLogout(){
        userController.logout(mockHttpSession);
        Mockito.verify(mockHttpSession, Mockito.times(1)).invalidate();
    }

    @Test
    @WithMockUser(username="user")
    public void testOpenAccessToGetSessionUser(){
        userController.getSessionUser();
        Mockito.verify(userManagement, Mockito.times(1)).getUser("user");
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToGetUser(){
        userController.get("someUsername");
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleToGetUser(){
        userController.get("someUsername");
        Mockito.verify(userManagement, Mockito.times(1)).getUser("someUsername");
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToGetUsers(){
        userController.getAll();
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleToGetUsers(){
        userController.getAll();
        Mockito.verify(userManagement, Mockito.times(1)).getUsers();
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToAddUser(){
        userController.add(CommonDtos.getValidUser());
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleAddUser(){
        User user = CommonDtos.getValidUser();
        userController.add(user);
        Mockito.verify(userManagement, Mockito.times(1)).addUser(user);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToDeleteUser(){
        userController.delete("");
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleDeleteUser(){
        userController.delete("someUsername");
        Mockito.verify(userManagement, Mockito.times(1)).delete("someUsername");
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToUpdateUser(){
        userController.update("", CommonDtos.getValidUser());
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleUpdateUser(){
        User user = CommonDtos.getValidUser();
        userController.update("someUsername", user);
        Mockito.verify(userManagement, Mockito.times(1)).updateUser("someUsername", user);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToChangePassword(){
        userController.changePassword("", "userPassword", "newPassword");
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleChangePassword(){
        userController.changePassword("user",
                "userPassword", "newPassword");
        Mockito.verify(userManagement, Mockito.times(1)).changePassword("user", "userPassword", "newPassword");
    }

    @Test
    @WithMockUser(username="user")
    public void testOpenAccessToUpdateSessionPassword(){
        userController.changePasswordSession("userPassword", "newPassword");
        Mockito.verify(userManagement, Mockito.times(1)).changePassword("user", "userPassword", "newPassword");
    }

    @Test
    @WithMockUser(username="user")
    public void testOpenAccessToUpdateSessionUser(){
        User user = CommonDtos.getValidUser();
        userController.updateSessionUser(user);
        Mockito.verify(userManagement, Mockito.times(1)).updateUser("user", user);
    }

    @Test
    public void testOpenAccessToSignUp(){
        User user = CommonDtos.getValidUser();
        userController.signUp(user,  CommonDtos.defaultValidPassword());
        Mockito.verify(userManagement, Mockito.times(1)).signUp(user, CommonDtos.defaultValidPassword());
    }

    @Test
    public void testOpenAccessToIsUsernameTaken(){
        userController.isUsernameTaken("user");
        Mockito.verify(userManagement, Mockito.times(1)).isUsernameTaken("user");
    }
}
