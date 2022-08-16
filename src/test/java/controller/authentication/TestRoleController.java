package controller.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.authentication.RoleController;
import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.NoUserInSessionException;
import com.imemalta.api.gourmetSnApp.services.authentication.RoleManagement;

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
public class TestRoleController {

    @MockBean
    private RoleManagement mockRoleManagement;

    @Autowired
    private RoleController roleController;

    @Test
    public void testPubliclyGetRolesDefinedInTheSystem(){
        roleController.getRoles();
        Mockito.verify(mockRoleManagement, Mockito.times(1)).getRoles();
    }

    @Test
    @WithMockUser(username="myUser",roles={})
    public void testGetUserSessionRoles(){
        roleController.getSessionUserRoles();
        Mockito.verify(mockRoleManagement, Mockito.times(1)).getUserRolesFromDatabase("myUser");
    }

    @Test(expected= NoUserInSessionException.class)
    public void testGetUserSessionRolesNotLoggedIn(){
        roleController.getSessionUserRoles();
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void denyAccessToGetUsersRolesIfSessionUsersHasInsufficientRoles(){
        roleController.getUserRoles("validUsername1");
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void allowAccessToGetUsersRolesIfSessionUsersHasSufficientRoles(){
        roleController.getUserRoles("validUsername1");
        Mockito.verify(mockRoleManagement, Mockito.times(1)).getUserRolesFromDatabase("validUsername1");
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void denyAccessToSetUsersRolesIfSessionUsersHasInsufficientRoles(){
        roleController.setUserRoles("validUsername1", new Long[]{});
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void allowAccessToSetUsersRolesIfSessionUsersHasSufficientRoles(){
        roleController.setUserRoles("validUsername1", new Long[] {});
        Mockito.verify(mockRoleManagement, Mockito.times(1)).setUserRoles("validUsername1", new Long[] {});
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void denyAccessToAddUsersRolesIfSessionUsersHasInsufficientRoles(){
        roleController.addRoleToUser("validUsername1", 0L);
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void allowAccessToAddUsersRoleIfSessionUsersHasSufficientRoles(){
        Mockito.when(mockRoleManagement.addRolesToUser(Mockito.any(String.class), Mockito.any(Long[].class))).thenReturn(new Role[]{new Role()});
        roleController.addRoleToUser("validUsername1", 0L);
        Mockito.verify(mockRoleManagement, Mockito.times(1)).addRolesToUser("validUsername1", new Long[] {0L});
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="user")
    public void denyAccessToRemoveUsersRolesIfSessionUsersHasInsufficientRoles(){
        roleController.removeRoleFromUser("validUsername1", 0L);
    }

    @Test
    @WithMockUser(username="user", authorities={"ROLE_MANAGE_USERS"})
    public void allowAccessToRemoveUsersRoleIfSessionUsersHasSufficientRoles(){
        roleController.removeRoleFromUser("validUsername1", 0L);
        Mockito.verify(mockRoleManagement, Mockito.times(1)).removeRolesFromUser("validUsername1", new Long[] {0L});
    }
}
