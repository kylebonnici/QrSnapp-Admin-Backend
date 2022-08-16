package controller.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.controllers.backend.QRCodeController;
import com.imemalta.api.gourmetSnApp.controllers.backend.QRCodeRulesController;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleRepository;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeManagement;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleManagement;
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
public class TestQrCodeRuleController {
    @Autowired
    private QRCodeRulesController qrCodeRulesController;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private QRCodeRuleRepository qrCodeRuleRepository;

    @MockBean
    private QRCodeRuleManagement qrCodeRuleManagement;

    @MockBean
    private QRCodeRuleResolver qrCodeRuleResolver;

    private QRCodeRule qrCodeRule;
    private QRCode qrCode;

    @Before
    public void setUp() {
        User user = CommonDtos.getValidUser();
        user.setUsername("owner");
        userRepository.save(user);

        Group group = CommonDtos.getValidGroup();
        group.setOwner(user);
        groupRepository.save(group);

        qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
    }

    @Test
    public void testResolve(){
        qrCodeRulesController.resolve(qrCode.getId().toString());
        Mockito.verify(qrCodeRuleResolver, Mockito.times(1)).resolveQrCode(qrCode.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetRuleUsageCountOwner(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.getRuleUsageCount(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleResolver, Mockito.times(1)).qrCodeRuleUsageCount(qrCodeRule.getId());
    }


    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetRuleUsageCountAccessDenied(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.getRuleUsageCount(qrCodeRule.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE"})
    public void tesGetRuleUsageCountSuperAdmin(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.getRuleUsageCount(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleResolver, Mockito.times(1)).qrCodeRuleUsageCount(qrCodeRule.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetOwner(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.get(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).get(qrCodeRule.getId());
    }


    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetAccessDenied(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.get(qrCodeRule.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE"})
    public void tesGetSuperAdmin(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.get(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).get(qrCodeRule.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testGetQrCodeRulesOwner(){
        qrCodeRulesController.getQrCodeRules(qrCode.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).getQRCodeRules(qrCode.getId());
    }


    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testGetQrCodeRulesAccessDenied(){
        qrCodeRulesController.getQrCodeRules(qrCode.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE"})
    public void tesGetQrCodeRulesSuperAdmin(){
        qrCodeRulesController.getQrCodeRules(qrCode.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).getQRCodeRules(qrCode.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testAddOwner(){
        qrCodeRulesController.add(qrCode.getId(), qrCodeRule);
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).add(qrCodeRule, qrCode.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testAddAccessDenied(){
        qrCodeRulesController.add(qrCode.getId(), qrCodeRule);
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_ADD_QRCODE_RULE"})
    public void tesAddSuperAdmin(){
        qrCodeRulesController.add(qrCode.getId(), qrCodeRule);
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).add(qrCodeRule, qrCode.getId());
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testUpdateOwner(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.update(qrCodeRule.getId(), qrCodeRule);
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).update(qrCodeRule.getId(), qrCodeRule);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testUpdateAccessDenied(){
        qrCodeRulesController.update(qrCodeRule.getId(), qrCodeRule);
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_EDIT_QRCODE_RULE"})
    public void tesUpdateSuperAdmin(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.update(qrCodeRule.getId(), qrCodeRule);
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).update(qrCodeRule.getId(), qrCodeRule);
    }

    @Test
    @WithMockUser(username="owner",roles={})
    public void testDeleteOwner(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.delete(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).delete(qrCodeRule.getId());
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username="notOwner",roles={""})
    public void testDeleteAccessDenied(){
        qrCodeRulesController.delete(qrCodeRule.getId());
    }

    @Test
    @WithMockUser(username="notOwner", authorities={"ROLE_SUPER_ADMIN_DELETE_QRCODE_RULE"})
    public void tesDeleteSuperAdmin(){
        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRulesController.delete(qrCodeRule.getId());
        Mockito.verify(qrCodeRuleManagement, Mockito.times(1)).delete(qrCodeRule.getId());
    }
}
