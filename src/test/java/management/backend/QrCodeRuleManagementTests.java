package management.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.enums.ComparisonType;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleManagement;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class QrCodeRuleManagementTests {
    @Autowired
    private QRCodeRuleManagement qrCodeRuleManagement;

    @Autowired
    private QRCodeRuleRepository qrCodeRuleRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private Group group;
    private QRCode qrCode;

    @Before
    public void setUp(){
        User user = CommonDtos.getValidUser();
        userRepository.save(user);

        group = CommonDtos.getValidGroup();
        group.setOwner(user);
        groupRepository.save(group);

        qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);
    }

    @Test
    public void testGetById() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        TestCase.assertEquals(qrCodeRule.getId(), qrCodeRuleManagement.get(qrCodeRule.getId()).getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetByIdNonExisting() {
        qrCodeRuleManagement.get(0);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetInvalidQrCodesRules() {
        qrCodeRuleManagement.getQRCodeRules(UUID.randomUUID());
    }

    @Test
    public void testGetQrCodesRules() {
        QRCode qrCode1 = CommonDtos.getValidQrCode();
        qrCode1.setGroup(group);
        qrCodeRepository.save(qrCode1);

        QRCode qrCode2 = CommonDtos.getValidQrCode();
        qrCode2.setGroup(group);
        qrCodeRepository.save(qrCode2);

        List<QRCodeRule> qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode1.getId());
        TestCase.assertEquals(0, qrCodeRules.size());

        qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode2.getId());
        TestCase.assertEquals(0, qrCodeRules.size());

        QRCodeRule qrCodeRule1 = CommonDtos.getValidQrCodeRule();
        qrCodeRule1.setQrCode(qrCode1);
        qrCodeRuleRepository.save(qrCodeRule1);

        qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode1.getId());
        TestCase.assertEquals(1, qrCodeRules.size());

        qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode2.getId());
        TestCase.assertEquals(0, qrCodeRules.size());

        QRCodeRule qrCodeRule2 = CommonDtos.getValidQrCodeRule();
        qrCodeRule2.setQrCode(qrCode2);
        qrCodeRuleRepository.save(qrCodeRule2);

        qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode1.getId());
        TestCase.assertEquals(1, qrCodeRules.size());

        qrCodeRules = qrCodeRuleManagement.getQRCodeRules(qrCode2.getId());
        TestCase.assertEquals(1, qrCodeRules.size());
    }

    @Test
    public void testAddValid() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();

        QRCodeRule qrCodeRule1 = qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());

        TestCase.assertEquals(qrCodeRule.getRedirectURL(), qrCodeRule1.getRedirectURL());
        TestCase.assertEquals(qrCodeRule.getFriendlyName(), qrCodeRule1.getFriendlyName());
        TestCase.assertEquals(qrCodeRule.getValidDays(), qrCodeRule1.getValidDays());
        TestCase.assertEquals(qrCodeRule.getValidFromDate(), qrCodeRule1.getValidFromDate());
        TestCase.assertEquals(qrCodeRule.getValidToDate(), qrCodeRule1.getValidToDate());
        TestCase.assertEquals(qrCodeRule.getValidToTime(), qrCodeRule1.getValidToTime());
        TestCase.assertEquals(qrCodeRule.getValidFromTime(), qrCodeRule1.getValidFromTime());
        TestCase.assertEquals(qrCodeRule.getMinCount(), qrCodeRule1.getMinCount());
        TestCase.assertEquals(qrCodeRule.getMaxCount(), qrCodeRule1.getMaxCount());
        TestCase.assertEquals(qrCodeRule.getComparisonType(), qrCodeRule1.getComparisonType());
        TestCase.assertEquals(qrCodeRule.getPriority(), qrCodeRule1.getPriority());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddValidEmptyRedirectURL() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setRedirectURL(null);

        qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddValidEmptyFriendlyName() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setFriendlyName(null);

        qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddValidEmptyValidDays() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setValidDays(null);

        qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddInValidQrCode() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();

        qrCodeRuleManagement.add(qrCodeRule, UUID.randomUUID());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddDuplicatePriority() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();

        qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());
        qrCodeRuleManagement.add(qrCodeRule, qrCode.getId());
    }

    @Test
    public void testUpdateValid() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        QRCodeRule modifiedQrCodeRule = new QRCodeRule();

        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        dayOfWeeks.add(DayOfWeek.THURSDAY);

        modifiedQrCodeRule.setRedirectURL("SomeUrl2");
        modifiedQrCodeRule.setFriendlyName("someNewFriendlyName");
        modifiedQrCodeRule.setValidDays(dayOfWeeks);
        modifiedQrCodeRule.setValidFromDate(LocalDate.of(2018, 1, 1));
        modifiedQrCodeRule.setValidToDate(LocalDate.of(2018, 1, 31));
        modifiedQrCodeRule.setValidFromTime(LocalTime.of(8, 30, 0));
        modifiedQrCodeRule.setValidToTime(LocalTime.of(14, 45, 59));
        modifiedQrCodeRule.setMaxCount(3);
        modifiedQrCodeRule.setMinCount(1);
        modifiedQrCodeRule.setComparisonType(ComparisonType.EXCLUSION);
        modifiedQrCodeRule.setPriority(5);

        QRCodeRule qrCodeRule1 = qrCodeRuleManagement.update(qrCodeRule.getId(), modifiedQrCodeRule);

        TestCase.assertEquals(modifiedQrCodeRule.getRedirectURL(), qrCodeRule1.getRedirectURL());
        TestCase.assertEquals(modifiedQrCodeRule.getFriendlyName(), qrCodeRule1.getFriendlyName());
        TestCase.assertEquals(modifiedQrCodeRule.getValidDays(), qrCodeRule1.getValidDays());
        TestCase.assertEquals(modifiedQrCodeRule.getValidFromDate(), qrCodeRule1.getValidFromDate());
        TestCase.assertEquals(modifiedQrCodeRule.getValidToDate(), qrCodeRule1.getValidToDate());
        TestCase.assertEquals(modifiedQrCodeRule.getValidToTime(), qrCodeRule1.getValidToTime());
        TestCase.assertEquals(modifiedQrCodeRule.getValidFromTime(), qrCodeRule1.getValidFromTime());
        TestCase.assertEquals(modifiedQrCodeRule.getMinCount(), qrCodeRule1.getMinCount());
        TestCase.assertEquals(modifiedQrCodeRule.getMaxCount(), qrCodeRule1.getMaxCount());
        TestCase.assertEquals(modifiedQrCodeRule.getComparisonType(), qrCodeRule1.getComparisonType());
        TestCase.assertEquals(modifiedQrCodeRule.getPriority(), qrCodeRule1.getPriority());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateDuplicatePriority() {
        {
            QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
            qrCodeRule.setQrCode(qrCode);
            qrCodeRule.setPriority(1);
            qrCodeRuleRepository.save(qrCodeRule);
        }

        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRule.setPriority(2);
        qrCodeRuleRepository.save(qrCodeRule);

        qrCodeRule.setPriority(1);
        qrCodeRuleManagement.update(qrCodeRule.getId(), qrCodeRule);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateValidEmptyRedirectURL() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        QRCodeRule changedQrCodeRule = CommonDtos.getValidQrCodeRule();
        changedQrCodeRule.setRedirectURL(null);

        qrCodeRuleManagement.add(changedQrCodeRule, qrCode.getId());
    }



    @Test(expected = ConstraintViolationException.class)
    public void testUpdateValidEmptyFriendlyName() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        QRCodeRule changedQrCodeRule = CommonDtos.getValidQrCodeRule();
        changedQrCodeRule.setFriendlyName(null);

        qrCodeRuleManagement.add(changedQrCodeRule, qrCode.getId());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateValidEmptyValidDays() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        QRCodeRule changedQrCodeRule = CommonDtos.getValidQrCodeRule();
        changedQrCodeRule.setValidDays(null);

        qrCodeRuleManagement.add(changedQrCodeRule, qrCode.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateInValidQrCode() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();

        qrCodeRuleManagement.update(0, qrCodeRule);
    }

    @Test
    public void testDeleteExistingById() {
        QRCodeRule qrCodeRule = CommonDtos.getValidQrCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRuleRepository.save(qrCodeRule);

        qrCodeRuleManagement.delete(qrCodeRule.getId());
        TestCase.assertTrue(qrCodeRuleRepository.findById(qrCodeRule.getId()).isEmpty());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNonExistingEstablishment() {
        qrCodeRuleManagement.delete(1);
    }
}
