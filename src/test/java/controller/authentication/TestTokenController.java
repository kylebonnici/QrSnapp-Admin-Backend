package controller.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.authentication.TokenController;
import com.imemalta.api.gourmetSnApp.services.authentication.TokenManagement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class TestTokenController {

    @MockBean
    private TokenManagement mockTokenManagement;

    @Autowired
    private TokenController tokenController;

    @Test
    public void testOpenAccessToActivateUserWithToken(){
        tokenController.activateUser("token");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).activateUser("token");
    }

    @Test
    public void testOpenAccessToResetPasswordWithToken(){
        tokenController.resetPassword("token","password");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).resetPassword("token", "password");
    }

    @Test
    public void testOpenAccessToRequestResetPasswordToken(){
        tokenController.requestPasswordResetToken("validUsername1");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).requestPasswordResetToken("validUsername1");
    }

    @Test
    public void testOpenAccessToRequestEmailActivationToken(){
        tokenController.sendEmailActivationToken("validUsername1");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).sendEmailActivationToken("validUsername1");
    }

    @Test
    public void testOpenAccessToActivateUserWithPasswordUsingToken(){
        tokenController.activateUserWithPassword("token", "password");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).activateUserWithPassword("token", "password");
    }

    @Test(expected= AccessDeniedException.class)
    @WithMockUser(username="user")
    public void testDenyOpenAccessToRequestFirstPasswordToken(){
        tokenController.requestFirstPasswordToken("validUsername1");
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void testAllowAccessWithAppropriateRoleToRequestFirstPasswordToken(){
        tokenController.requestFirstPasswordToken("validUsername1");
        Mockito.verify(mockTokenManagement, Mockito.times(1)).requestFirstPasswordToken("validUsername1");
    }


}
