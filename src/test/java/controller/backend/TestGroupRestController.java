package controller.backend;


import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.backend.GroupController;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.services.backend.GroupManagement;
import helpers.CommonDtos;
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
public class TestGroupRestController {

    @Autowired
    private GroupController groupController;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private GroupManagement groupManagement;

    @Test
    public void testGetEstablishment(){
        groupController.get(1);
        Mockito.verify(groupManagement, Mockito.times(1)).get(1);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testAddGroupOwner(){
        Group group = CommonDtos.getValidGroup();
        groupController.add(group, "owner");
        Mockito.verify(groupManagement, Mockito.times(1)).add(group,"owner");
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testAddGroupAccessDenied(){
        groupController.add(CommonDtos.getValidGroup(),"owner");
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_ADD_GROUP"})
    public void testAddGroupSuperAdmin(){
        Group group = CommonDtos.getValidGroup();
        groupController.add(group, "owner");
        Mockito.verify(groupManagement, Mockito.times(1)).add(group, "owner");
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetOwnerGroups(){
        groupController.getOwnersGroup("owner");
        Mockito.verify(groupManagement, Mockito.times(1)).getOwnersGroups("owner");

    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetOwnerGroupsAccessDenied(){
        groupController.getOwnersGroup("owner");
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_OWNERS_GROUP"})
    public void testGetOwnersGroupsSuperAdmin(){
        groupController.getOwnersGroup("owner");
        Mockito.verify(groupManagement, Mockito.times(1)).getOwnersGroups("owner");
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testUpdateGroups(){
        Group group = CommonDtos.getValidGroup();
        User user = CommonDtos.getValidUser();
        user.setUsername("owner");
        userRepository.save(user);
        group.setOwner(user);
        groupRepository.save(group);

        groupController.update(group.getId(), group);
        Mockito.verify(groupManagement, Mockito.times(1)).update(group.getId(), group);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testUpdateAccessDenied(){
        groupController.update(1, CommonDtos.getValidGroup());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_EDIT_GROUP"})
    public void testUpdateSuperAdmin(){
        Group group = CommonDtos.getValidGroup();
        User user = CommonDtos.getValidUser();
        user.setUsername("owner");
        userRepository.save(user);
        group.setOwner(user);
        groupRepository.save(group);

        groupController.update(group.getId(), group);
        Mockito.verify(groupManagement, Mockito.times(1)).update(group.getId(), group);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testDeleteGroups(){
        Group group = CommonDtos.getValidGroup();
        User user = CommonDtos.getValidUser();
        user.setUsername("owner");
        userRepository.save(user);
        group.setOwner(user);
        groupRepository.save(group);

        groupController.delete(group.getId());
        Mockito.verify(groupManagement, Mockito.times(1)).delete(group.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testDeleteAccessDenied(){
        groupController.delete(1);
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_DELETE_GROUP"})
    public void testDeleteSuperAdmin(){
        Group group = CommonDtos.getValidGroup();
        User user = CommonDtos.getValidUser();
        user.setUsername("owner");
        userRepository.save(user);
        group.setOwner(user);
        groupRepository.save(group);

        groupController.delete(group.getId());
        Mockito.verify(groupManagement, Mockito.times(1)).delete(group.getId());
    }

}
