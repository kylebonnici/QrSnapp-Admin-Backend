package management.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.UserNotFoundException;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.services.backend.GroupManagement;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class GroupManagementTests {
    @Autowired
    private GroupManagement groupManagement;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test(expected = EntityNotFoundException.class)
    public void testGetById_InvalidId() {
        groupManagement.get(1);
    }

    @Test
    public void testGetById() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        Group group = CommonDtos.getValidGroup();
        groupRepository.save(group);

        Group loadedGroup = groupManagement.get(group.getId());
        TestCase.assertEquals(group.getName(), loadedGroup.getName());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddGroupNameNull_ConstraintViolation() {
        Group group = CommonDtos.getValidGroup();
        group.setName(null);

        groupManagement.add(group, "SomeWrongUserName");
    }

    @Test
    public void testAddEstablishmentValidOwner() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        Group group = groupManagement.add(CommonDtos.getValidGroup(), user.getUsername());
        TestCase.assertTrue(groupRepository.findById(group.getId()).isPresent());
    }

    @Test(expected = UserNotFoundException.class)
    public void testAddEstablishmentInvalidOwner() {
        groupManagement.add(CommonDtos.getValidGroup(), "WrongUserName");
    }

    @Test
    public void testGetAllGroups_NoGroups() {
        List<Group> groups = groupManagement.getAll();
        TestCase.assertEquals(0, groups.size());
    }

    @Test
    public void testGetAllGroups() {
        User user = CommonDtos.getValidUser();
        userRepository.save(CommonDtos.getValidUser());

        groupManagement.add(CommonDtos.getValidGroup(), user.getUsername());
        List<Group> groups = groupManagement.getAll();
        TestCase.assertEquals(1, groups.size());

        groupManagement.add(CommonDtos.getValidGroup(), user.getUsername());
        groups  = groupManagement.getAll();
        TestCase.assertEquals(2, groups.size());
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetMyGroups_InvalidUser() {
        groupManagement.getOwnersGroups("someWrongUsername");
    }

    @Test
    public void testGetMyGroups_None() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        List<Group> groups = groupManagement.getOwnersGroups(user.getUsername());
        TestCase.assertEquals(0, groups.size());
    }


    @Test
    public void testGetMyGroups() {
        User user1 = CommonDtos.getValidUser();
        user1.setUsername(user1.getUsername() + "1");
        user1.setEmail(user1.getEmail() + "m");
        userRepository.save(user1);

        User user2 = CommonDtos.getValidUser();
        user2.setUsername(user2.getUsername() + "2");
        user2.setEmail(user2.getEmail() + "mm");
        userRepository.save(user2);

        groupManagement.add(CommonDtos.getValidGroup(), user1.getUsername());

        List<Group> groups = groupManagement.getOwnersGroups(user1.getUsername());
        TestCase.assertEquals(1, groups.size());

        groups = groupManagement.getOwnersGroups(user2.getUsername());
        TestCase.assertEquals(0, groups.size());

        groupManagement.add(CommonDtos.getValidGroup(), user2.getUsername());

        groups = groupManagement.getOwnersGroups(user1.getUsername());
        TestCase.assertEquals(1, groups.size());

        groups = groupManagement.getOwnersGroups(user2.getUsername());
        TestCase.assertEquals(1, groups.size());
    }

    @Test(expected = ConstraintViolationException.class)
    public void tesUpdateGroupNameNull_ConstraintViolation() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        Group group = CommonDtos.getValidGroup();
        groupRepository.save(group);

        group.setName(null);

        groupManagement.update(group.getId(), group);
    }

    @Test
    public void tesUpdateGroupValid() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        Group wasGroup = CommonDtos.getValidGroup();
        groupRepository.save(wasGroup);

        Group updatedGroup = CommonDtos.getValidGroup();
        updatedGroup.setName(wasGroup.getName() + "2");

        Group latestGroup = groupManagement.update(wasGroup.getId(), updatedGroup);
        TestCase.assertEquals(updatedGroup.getName(), latestGroup.getName());
    }


    @Test(expected = EntityNotFoundException.class)
    public void tesUpdateNonExistingGroup() {
        groupManagement.update(1, CommonDtos.getValidGroup());
    }

    @Test
    public void tesChangeOwnerValid() {
        User user1 = CommonDtos.getValidUser();
        user1.setUsername(user1.getUsername() + "1");
        user1.setEmail(user1.getEmail() + "m");
        userRepository.save(user1);

        User user2 = CommonDtos.getValidUser();
        user2.setUsername(user2.getUsername() + "2");
        user2.setEmail(user2.getEmail() + "mm");
        userRepository.save(user2);

        List<Group> groups = groupManagement.getOwnersGroups(user1.getUsername());
        TestCase.assertEquals(0, groups.size());

        groups = groupManagement.getOwnersGroups(user2.getUsername());
        TestCase.assertEquals(0, groups.size());

        Group savedGroup = groupManagement.add(CommonDtos.getValidGroup(), user1.getUsername());

        groups = groupManagement.getOwnersGroups(user1.getUsername());
        TestCase.assertEquals(1, groups.size());

        groups = groupManagement.getOwnersGroups(user2.getUsername());
        TestCase.assertEquals(0, groups.size());

        groupManagement.changeOwner(savedGroup.getId(), user2.getUsername());

        groups = groupManagement.getOwnersGroups(user1.getUsername());
        TestCase.assertEquals(0, groups.size());

        groups = groupManagement.getOwnersGroups(user2.getUsername());
        TestCase.assertEquals(1, groups.size());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testChangeOwnerInValidGroup() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        groupManagement.changeOwner(1, user.getUsername());
    }

    @Test(expected = UserNotFoundException.class)
    public void testChangeOwnerInValidNewOwner() {
        groupManagement.changeOwner(1, "someUserName");
    }

    @Test
    public void testDeleteExistingGroupById() {
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        Group group = CommonDtos.getValidGroup();
        groupRepository.save(group);

        groupManagement.delete(group.getId());
        TestCase.assertTrue(groupRepository.findById(group.getId()).isEmpty());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNonExistingGroup() {
        groupManagement.delete(1);
    }
}
