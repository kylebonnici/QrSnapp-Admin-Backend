package management.authentication;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.UserMetadataRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.ChangeOwnUserRolesException;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.UserNotFoundException;
import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.RoleRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.services.authentication.RoleManagement;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=Main.class)
@Transactional(transactionManager="transactionManager")
public class TestRoleManagement {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMetadataRepository userMetadataRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleManagement roleManagement;

    @Before
    public void setUp() {
        createRoles();
        signUpValidUser();
    }

    @After
    public void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test(expected= UserNotFoundException.class)
    @WithMockUser(username="validUsername1")
    public void testSetUserRolesWrongUsername() {
        roleManagement.setUserRoles("wrongUsername", null);
    }

    @Test(expected= UserNotFoundException.class)
    @WithMockUser(username="validUsername1")
    public void testAddUserRolesWrongUsername() {
        roleManagement.addRolesToUser("wrongUsername", null);
    }

    @Test(expected= UserNotFoundException.class)
    @WithMockUser(username="validUsername1")
    public void testDeleteUserRolesWrongUsername() {
        roleManagement.removeRolesFromUser("wrongUsername", null);
    }

    @Test(expected= ChangeOwnUserRolesException.class)
    @WithMockUser(username="validUsername1")
    public void testSetUserRolesSameUser() {
        roleManagement.setUserRoles("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW", "SALES_PERSON"}));
    }

    @Test(expected= ChangeOwnUserRolesException.class)
    @WithMockUser(username="validUsername1")
    public void testAddUserRolesSameUser() {
        roleManagement.addRolesToUser("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW", "SALES_PERSON"}));
    }

    @Test(expected= ChangeOwnUserRolesException.class)
    @WithMockUser(username="validUsername1")
    public void testDeleteUserRolesSameUser() {
        roleManagement.removeRolesFromUser("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW", "SALES_PERSON"}));
    }

    @Test
    @WithMockUser(username="user")
    public void testSetUserRoles() {
        roleManagement.setUserRoles("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW", "SALES_PERSON"}));

        Optional<User> user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(3, user.get().getRoles().size());

        roleManagement.setUserRoles("validUsername1", getRoleIds(new String[]{"VIEW", "SALES_PERSON"}));

        user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(2, user.get().getRoles().size());
    }

    @Test
    @WithMockUser(username="user")
    public void tesAddUserRoles() {
        roleManagement.addRolesToUser("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW"}));

        Optional<User> user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(2, user.get().getRoles().size());

        roleManagement.addRolesToUser("validUsername1", getRoleIds(new String[]{"VIEW", "SALES_PERSON"}));

        user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(3, user.get().getRoles().size());
    }

    @Test
    @WithMockUser(username="user")
    public void tesDeleteUserRoles() {
        roleManagement.addRolesToUser("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW", "SALES_PERSON"}));

        Optional<User> user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(3, user.get().getRoles().size());

        roleManagement.removeRolesFromUser("validUsername1", getRoleIds(new String[]{"VIEW", "SALES_PERSON"}));

        user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertEquals(1, user.get().getRoles().size());

        roleManagement.removeRolesFromUser("validUsername1", getRoleIds(new String[]{"ADMIN"}));

        user = userRepository.findByUsername("validUsername1");
        TestCase.assertTrue(user.isPresent());
        TestCase.assertNull(user.get().getRoles());
    }

    @Test
    @WithMockUser(username="user")
    public void tesGetUserRoles() {
        roleManagement.addRolesToUser("validUsername1", getRoleIds(new String[]{"ADMIN", "VIEW"}));

        Set<Role> roles = roleManagement.getUserRolesFromDatabase("validUsername1");
        TestCase.assertEquals(2, roles.size());
        TestCase.assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ADMIN")));
        TestCase.assertTrue(roles.stream().anyMatch(r -> r.getName().equals("VIEW")));
    }

    @Test
    public void tesGetRoles() {
        List<Role> roles = roleManagement.getRoles();
        TestCase.assertEquals(3, roles.size());
        TestCase.assertTrue(roles.stream().anyMatch(r -> r.getName().equals("ADMIN")));
        TestCase.assertTrue(roles.stream().anyMatch(r -> r.getName().equals("VIEW")));
        TestCase.assertTrue(roles.stream().anyMatch(r -> r.getName().equals("SALES_PERSON")));
    }

    private void createRoles(){
        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator");

        roleRepository.save(role);

        role = new Role();
        role.setName("VIEW");
        role.setDescription("Viewer");

        roleRepository.save(role);

        role = new Role();
        role.setName("SALES_PERSON");
        role.setDescription("Sales Person");

        roleRepository.save(role);
    }

    private void signUpValidUser(){
        User user = CommonDtos.getValidUser();
        UserMetadata userMetadata =  new UserMetadata();

        user.setName("someName");
        user.setSurname("someSurname");
        user.setUsername("validUsername1");
        user.setUserMetadata(userMetadata);
        userMetadata.setPassword("okL3ngth");
        user.setUserMetadata(userMetadata);
        userMetadata.setUser(user);
        user.setEmail("email1@email.com");
        user.setAccountState(AccountState.PENDING_ACTIVATION);

        userRepository.save(user);
        userMetadataRepository.save(userMetadata);
    }

    private long getRoleId(String roleName){
        return ((Role)roleRepository.findByNameIn(new String[]  {roleName}).toArray()[0]).getId();
    }

    private Long[] getRoleIds(String[] roles) {
        Long[] roleNamesIdsArr = new Long[roles.length];

        for (int i = 0; i < roles.length; i++) {
            roleNamesIdsArr[i] = getRoleId(roles[i]);
        }

        return roleNamesIdsArr;
    }
}
