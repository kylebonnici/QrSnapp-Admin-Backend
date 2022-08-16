package management.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeManagement;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class QRCodeManagementTests {
    private QRCodeManagement qrCodeManagement;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Group group;

    @Before
    public void setUp() {
        qrCodeManagement = new QRCodeManagement(qrCodeRepository,groupRepository);
        user = CommonDtos.getValidUser();
        userRepository.save(user);

        group = CommonDtos.getValidGroup();
        group.setOwner(user);
        groupRepository.save(group);
    }

    @Test
    public void testGetById() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        QRCode qrCode1 = qrCodeManagement.get(qrCode.getId());
        TestCase.assertEquals(qrCode.getId(), qrCode1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetByIdNonExisting() {
        qrCodeManagement.get(UUID.randomUUID());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGroupQrCodesNonExisting() {
        qrCodeManagement.getGroupsQRCodes(1);
    }

    @Test
    public void testGroupQrCodes() {
        Group group1 = CommonDtos.getValidGroup();
        group1.setOwner(user);
        groupRepository.save(group1);

        Group group2 = CommonDtos.getValidGroup();
        group2.setOwner(user);
        groupRepository.save(group2);

        List<QRCode> qrCodes = qrCodeManagement.getGroupsQRCodes(group1.getId());
        TestCase.assertEquals(0, qrCodes.size());

        qrCodes = qrCodeManagement.getGroupsQRCodes(group2.getId());
        TestCase.assertEquals(0, qrCodes.size());

        QRCode qrCode1 = CommonDtos.getValidQrCode();
        qrCode1.setGroup(group1);
        qrCodeRepository.save(qrCode1);

        qrCodes = qrCodeManagement.getGroupsQRCodes(group1.getId());
        TestCase.assertEquals(1, qrCodes.size());

        qrCodes = qrCodeManagement.getGroupsQRCodes(group2.getId());
        TestCase.assertEquals(0, qrCodes.size());

        QRCode qrCode2 = CommonDtos.getValidQrCode();
        qrCode2.setGroup(group2);
        qrCodeRepository.save(qrCode2);

        qrCodes = qrCodeManagement.getGroupsQRCodes(group1.getId());
        TestCase.assertEquals(1, qrCodes.size());

        qrCodes = qrCodeManagement.getGroupsQRCodes(group2.getId());
        TestCase.assertEquals(1, qrCodes.size());
    }

    @Test
    public void testAddValid(){
        Group group = CommonDtos.getValidGroup();
        group.setOwner(user);
        groupRepository.save(group);

        QRCode qrCode = CommonDtos.getValidQrCode();

        QRCode qrCode1 = qrCodeManagement.add(group.getId(), qrCode);
        TestCase.assertEquals(qrCode.getFriendlyName(), qrCode1.getFriendlyName());
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddEmptyFriendlyName(){
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setFriendlyName(null);

        qrCodeManagement.add(group.getId(), qrCode);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddEmptyDefaultRedirect(){
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setDefaultRedirect(null);

        qrCodeManagement.add(group.getId(), qrCode);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testAddEmptyZoneId(){
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setZoneId(null);

        qrCodeManagement.add(group.getId(), qrCode);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddInValidEstablishment(){
        qrCodeManagement.add(0, CommonDtos.getValidQrCode());
    }

    @Test
    public void testUpdateValid() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        QRCode updatedQrCode = CommonDtos.getValidQrCode();
        updatedQrCode.setFriendlyName("SomeDiffName");

        QRCode qrCode1 = qrCodeManagement.update(qrCode.getId(), updatedQrCode);
        TestCase.assertEquals(updatedQrCode.getFriendlyName(), qrCode1.getFriendlyName());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateInValidQrCode() {
        QRCode updatedQrCode = CommonDtos.getValidQrCode();
        qrCodeManagement.update(UUID.randomUUID(), updatedQrCode);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateEmptyFriendlyName() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        QRCode updateQrCode = CommonDtos.getValidQrCode();
        updateQrCode.setFriendlyName(null);

        qrCodeManagement.update(qrCode.getId(), updateQrCode);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateEmptyDefaultRedirect() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        QRCode updateQrCode = CommonDtos.getValidQrCode();
        updateQrCode.setDefaultRedirect(null);

        qrCodeManagement.update(qrCode.getId(), updateQrCode);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testUpdateEmptyZoneId() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        QRCode updateQrCode = CommonDtos.getValidQrCode();
        updateQrCode.setZoneId(null);

        qrCodeManagement.update(qrCode.getId(), updateQrCode);
    }

    @Test
    public void testDeleteExistingById() {
        QRCode qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);

        qrCodeManagement.delete(qrCode.getId());
        TestCase.assertTrue(qrCodeRepository.findById(qrCode.getId()).isEmpty());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNonExistingEstablishment() {
        qrCodeManagement.delete(UUID.randomUUID());
    }
}
