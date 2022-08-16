package controller.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.backend.QRCodeController;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeManagement;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleResolver;
import helpers.CommonDtos;
import org.junit.Before;
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
public class TestQrCodeController {
    @Autowired
    private QRCodeController qrCodeController;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @MockBean
    private QRCodeManagement qrCodeManagement;

    @MockBean
    private QRCodeRuleResolver qrCodeRuleResolver;

    private Group group;
    private QRCode qrCode;

    @Before
    public void setUp() {
        User user = CommonDtos.getValidUser();
        group = CommonDtos.getValidGroup();

        user.setUsername("owner");
        userRepository.save(user);

        group.setOwner(user);
        groupRepository.save(group);

        qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetQrCodeUsageCountOwner(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getQrCodeUsageCount(qrCode.getId());
        Mockito.verify(qrCodeRuleResolver, Mockito.times(1)).qrCodeUsageCount(qrCode.getId());
    }


    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetQrCodeUsageCountAccessDenied(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getQrCodeUsageCount(qrCode.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP"})
    public void tesGetQrCodeUsageCountSuperAdmin(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getQrCodeUsageCount(qrCode.getId());
        Mockito.verify(qrCodeRuleResolver, Mockito.times(1)).qrCodeUsageCount(qrCode.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetOwner(){
        qrCodeRepository.save(qrCode);
        qrCodeController.get(qrCode.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).get(qrCode.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetAccessDenied(){
        qrCodeRepository.save(qrCode);
        qrCodeController.get(qrCode.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP"})
    public void tesGetSuperAdmin(){
        qrCodeRepository.save(qrCode);
        qrCodeController.get(qrCode.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).get(qrCode.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetGroupQrCodeOwner(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getGroupQrCodes(group.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).getGroupsQRCodes(group.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetGroupQrCodeAccessDenied(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getGroupQrCodes(group.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP"})
    public void tesGetGroupQrCodeSuperAdmin(){
        qrCodeRepository.save(qrCode);
        qrCodeController.getGroupQrCodes(group.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).getGroupsQRCodes(group.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testAddQrCodeOwner(){
        qrCodeController.add(group.getId(), qrCode);
        Mockito.verify(qrCodeManagement, Mockito.times(1)).add(group.getId(), qrCode);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testAddGroupAccessDenied(){
        qrCodeController.add(group.getId(), qrCode);
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_ADD_QRCODES_TO_GROUP"})
    public void testAddGroupSuperAdmin(){
        qrCodeController.add(group.getId(), qrCode);
        Mockito.verify(qrCodeManagement, Mockito.times(1)).add(group.getId(), qrCode);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testUpdateQrCodeOwner(){
        qrCodeRepository.save(qrCode);
        qrCodeController.update(qrCode.getId(), qrCode);
        Mockito.verify(qrCodeManagement, Mockito.times(1)).update(qrCode.getId(), qrCode);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testUpdateQrCodeAccessDenied(){
        qrCodeRepository.save(qrCode);
        qrCodeController.update(qrCode.getId(), qrCode);
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_EDIT_QRCODES_TO_GROUP"})
    public void tesUpdateQrCodeSuperAdmin(){
        qrCodeRepository.save(qrCode);
        qrCodeController.update(qrCode.getId(), qrCode);
        Mockito.verify(qrCodeManagement, Mockito.times(1)).update(qrCode.getId(), qrCode);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testDeleteQrCodeOwner(){
        qrCodeRepository.save(qrCode);
        qrCodeController.delete(qrCode.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).delete(qrCode.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testDeleteQrCodeAccessDenied(){
        qrCodeRepository.save(qrCode);
        qrCodeController.delete(qrCode.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_DELETE_QRCODES_TO_GROUP"})
    public void tesDeleteQrCodeSuperAdmin(){
        qrCodeRepository.save(qrCode);
        qrCodeController.delete(qrCode.getId());
        Mockito.verify(qrCodeManagement, Mockito.times(1)).delete(qrCode.getId());
    }
}
