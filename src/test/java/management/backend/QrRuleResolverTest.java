package management.backend;

import com.imemalta.api.gourmetSnApp.Main;
import com.imemalta.api.gourmetSnApp.dtos.ResolveMetaDataDTO;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.enums.ComparisonType;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.*;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.exceptions.common.UnpaidSubscriptionException;
import com.imemalta.api.gourmetSnApp.services.SystemConfiguration;
import com.imemalta.api.gourmetSnApp.services.TimeService;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleResolver;
import com.imemalta.api.gourmetSnApp.services.backend.StripeSubscriptionManagement;
import helpers.CommonDtos;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Main.class)
@Transactional(transactionManager="transactionManager")
public class QrRuleResolverTest {
    private QRCodeRuleResolver qrCodeRuleResolver;

    @Autowired
    private QRCodeRuleRepository qrCodeRuleRepository;

    @Autowired
    private QRCodeRuleUsageRepository qrCodeRuleUsageRepository;

    @Autowired
    private QRCodeRepository qrCodeRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Mock
    private StripeSubscriptionManagement stripeSubscriptionManagement;

    @Mock
    private SystemConfiguration systemConfiguration;

    @Mock
    private TimeService timeService;

    private QRCode qrCode;
    private User user;

    private final Set<DayOfWeek> dayOfWeeks = new HashSet<>(Collections.singletonList(DayOfWeek.MONDAY));

    @Before
    public void setUp(){
        systemConfiguration.setSendTokenEmails(false);

        qrCodeRuleResolver = new QRCodeRuleResolver(timeService, qrCodeRuleRepository, qrCodeRepository, qrCodeRuleUsageRepository, stripeSubscriptionManagement);

        user = CommonDtos.getValidUser();
        UserMetadata userMetadata = new UserMetadata();
        user.setUserMetadata(userMetadata);
        userMetadata.setUser(user);
        userMetadata.setQrCodeSubscriptionActive(true);

        userRepository.save(user);
        userMetadataRepository.save(userMetadata);

        Group group = CommonDtos.getValidGroup();
        group.setOwner(user);
        groupRepository.save(group);

        qrCode = CommonDtos.getValidQrCode();
        qrCode.setGroup(group);
        qrCodeRepository.save(qrCode);
    }

    @Test(expected = UnpaidSubscriptionException.class)
    public void testQRCodeResolverUnpaidSubscription() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        user.getUserMetadata().setQrCodeSubscriptionActive(false);

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverCountByQrCodeRuleNotFound() {
        qrCodeRuleResolver.qrCodeRuleUsageCount(0);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverDisabled() {
        qrCode.setEnabled(false);
        QRCodeRule qrCodeRule = new QRCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRule.setEnabled(true);

        qrCodeRule.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule.setRedirectURL("https://someurl.com");
        qrCodeRule.setValidDays(dayOfWeeks);
        qrCodeRule.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule);
        qrCodeRuleResolver.resolveQrCode(qrCode.getId());
    }

    @Test
    public void testQRCodeRuleResolverDisabled() {
        qrCode.setEnabled(true);
        QRCodeRule qrCodeRule = new QRCodeRule();
        qrCodeRule.setQrCode(qrCode);
        qrCodeRule.setEnabled(false);

        qrCodeRule.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule.setRedirectURL("https://someurl.com");
        qrCodeRule.setValidDays(dayOfWeeks);
        qrCodeRule.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        ResolveMetaDataDTO metadata = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(),metadata.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountByQrCodeRule() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);
        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(23, 59));
        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);
        TestCase.assertEquals(0, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId()));

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId()));

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(2, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverCountByQrCodeRuleWithDateRangeNotFound() {
        qrCodeRuleResolver.qrCodeRuleUsageCount(0,
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59)));
    }

    @Test
    public void testQRCodeResolverCountByQrCodeRuleWithDateRange() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);
        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(23, 59));
        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59, 59)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59, 59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59, 59)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59, 59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());


        TestCase.assertEquals(0, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0, 0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59, 58))));

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0, 0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59, 59))));

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0, 0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(15,59, 58))));

        TestCase.assertEquals(2, qrCodeRuleResolver.qrCodeRuleUsageCount(qrCodeRuleMonday.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0, 0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(15,59, 59))));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverCountByQrCodeNotFound() {
        qrCodeRuleResolver.qrCodeUsageCount(UUID.randomUUID());
    }

    @Test
    public void testQRCodeResolverCountByQrCode() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(13, 59));
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        QRCodeRule qrCodeRule2 = new QRCodeRule();
        qrCodeRule2.setQrCode(qrCode);
        qrCodeRule2.setEnabled(true);

        qrCodeRule2.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule2.setRedirectURL("https://someurl.com");
        qrCodeRule2.setValidDays(dayOfWeeks);
        qrCodeRule2.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule2.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule2.setValidFromTime(LocalTime.of(14, 0));
        qrCodeRule2.setValidToTime(LocalTime.of(16, 59));
        qrCodeRule2.setPriority(1);

        qrCodeRuleRepository.save(qrCodeRule2);

        TestCase.assertEquals(0, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId()));

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId()));

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(2, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverCountByQrCodeNotFoundWithDateRange() {
        qrCodeRuleResolver.qrCodeUsageCount(UUID.randomUUID(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59)));
    }

    @Test
    public void testQRCodeResolverCountByQrCodeWithDateRange() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(13, 59));
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        QRCodeRule qrCodeRule2 = new QRCodeRule();
        qrCodeRule2.setQrCode(qrCode);
        qrCodeRule2.setEnabled(true);

        qrCodeRule2.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule2.setRedirectURL("https://someurl.com");
        qrCodeRule2.setValidDays(dayOfWeeks);
        qrCodeRule2.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule2.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule2.setValidFromTime(LocalTime.of(14, 0));
        qrCodeRule2.setValidToTime(LocalTime.of(16, 59));
        qrCodeRule2.setPriority(1);



        qrCodeRuleRepository.save(qrCodeRule2);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59, 59)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59, 59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59, 59)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59,59)));

        qrCodeRuleResolver.resolveQrCode(qrCode.getId());

        TestCase.assertEquals(0, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59, 58))));

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(12,59, 59))));

        TestCase.assertEquals(1, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(15,59, 58))));

        TestCase.assertEquals(2, qrCodeRuleResolver.qrCodeUsageCount(qrCode.getId(),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(0,0)),
                LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.of(15,59, 59))));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testQRCodeResolverInvalidQrCode() {
        qrCodeRuleResolver.resolveQrCode(UUID.randomUUID());
    }

    @Test
    public void testQRCodeResolverCountRuleBetween() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(3);
        qrCodeRule1.setComparisonType(ComparisonType.BETWEEN);
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(17,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleExclusion() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(3);
        qrCodeRule1.setComparisonType(ComparisonType.EXCLUSION);
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(17,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleEqual() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(0);
        qrCodeRule1.setComparisonType(ComparisonType.EQUAL);
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleCountCountPersistenceDuration_InRange() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(0);
        qrCodeRule1.setComparisonType(ComparisonType.EQUAL);
        qrCodeRule1.setPriority(0);
        qrCodeRule1.setCountPersistenceDuration(4); //hrs

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,0)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,0)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,0)));
        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,0)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleCountCountPersistenceDuration_OutOfRange() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(0);
        qrCodeRule1.setComparisonType(ComparisonType.EQUAL);
        qrCodeRule1.setPriority(0);
        qrCodeRule1.setCountPersistenceDuration(4); //hrs

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,0)));
        Mockito.when(timeService.getTime((qrCode.getZoneId()))).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,0)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime()).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,1)));
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,1)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleGreater() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(0);
        qrCodeRule1.setComparisonType(ComparisonType.GREATER);
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(17,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverCountRuleLess() {
        QRCodeRule qrCodeRule1 = new QRCodeRule();
        qrCodeRule1.setQrCode(qrCode);
        qrCodeRule1.setEnabled(true);

        qrCodeRule1.setFriendlyName("qrCodeRuleMonday");
        qrCodeRule1.setRedirectURL("https://someurl.com");
        qrCodeRule1.setValidDays(dayOfWeeks);
        qrCodeRule1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule1.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule1.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRule1.setValidToTime(LocalTime.of(23, 59));
        qrCodeRule1.setMinCount(2);
        qrCodeRule1.setMaxCount(0);
        qrCodeRule1.setComparisonType(ComparisonType.LESS);
        qrCodeRule1.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRule1);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(12,59)));

        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRule1.getRedirectURL(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(15,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(16,59)));

        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverNoRules(){
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverBeforeFromDate() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);
        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(23, 59));

        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2017, 12, 31),
                LocalTime.of(23,59)));
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverAfterToDate() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);
        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(23, 59));

        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);

        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 2, 1),
                LocalTime.of(0,0)));
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverDayOfWeekTest() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);
        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(0, 0));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(23, 59));

        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);

        // Miss Rule 2 Jan was Tue
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 2),
                LocalTime.of(0,0)));
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        // Find 1st was Monday
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(0,0)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRuleMonday.getRedirectURL(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverTimeTest() {
        QRCodeRule qrCodeRuleMonday = new QRCodeRule();
        qrCodeRuleMonday.setQrCode(qrCode);
        qrCodeRuleMonday.setEnabled(true);

        qrCodeRuleMonday.setFriendlyName("qrCodeRuleMonday");
        qrCodeRuleMonday.setRedirectURL("https://someurl.com");
        qrCodeRuleMonday.setValidDays(dayOfWeeks);

        qrCodeRuleMonday.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleMonday.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleMonday.setValidFromTime(LocalTime.of(8, 30));
        qrCodeRuleMonday.setValidToTime(LocalTime.of(14, 45));
        qrCodeRuleMonday.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleMonday);

        // Miss Rule out of From
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(8,29)));
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        // Just In rule (from)
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(8,30)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRuleMonday.getRedirectURL(), redirectURL.getRedirectURL());

        // Just In rule (to)
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(14,45)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRuleMonday.getRedirectURL(), redirectURL.getRedirectURL());

        // Miss Rule out of To
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(14,46)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }

    @Test
    public void testQRCodeResolverPriorityTest() {
        QRCodeRule qrCodeRuleP1 = new QRCodeRule();
        qrCodeRuleP1.setQrCode(qrCode);
        qrCodeRuleP1.setFriendlyName("P1");
        qrCodeRuleP1.setRedirectURL("https://someurlP1.com");
        qrCodeRuleP1.setValidDays(dayOfWeeks);
        qrCodeRuleP1.setEnabled(true);

        qrCodeRuleP1.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleP1.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleP1.setValidFromTime(LocalTime.of(8, 30));
        qrCodeRuleP1.setValidToTime(LocalTime.of(14, 45));

        qrCodeRuleP1.setPriority(1);

        qrCodeRuleRepository.save(qrCodeRuleP1);

        QRCodeRule qrCodeRuleP0 = new QRCodeRule();
        qrCodeRuleP0.setQrCode(qrCode);
        qrCodeRuleP0.setFriendlyName("P0");
        qrCodeRuleP0.setRedirectURL("https://someurlP0.com");
        qrCodeRuleP0.setValidDays(dayOfWeeks);
        qrCodeRuleP0.setEnabled(true);

        qrCodeRuleP0.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleP0.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleP0.setValidFromTime(LocalTime.of(8, 30));
        qrCodeRuleP0.setValidToTime(LocalTime.of(14, 45));

        qrCodeRuleP0.setPriority(0);

        qrCodeRuleRepository.save(qrCodeRuleP0);

        QRCodeRule qrCodeRuleP2 = new QRCodeRule();
        qrCodeRuleP2.setQrCode(qrCode);
        qrCodeRuleP2.setFriendlyName("P2");
        qrCodeRuleP2.setRedirectURL("https://someurlP2.com");
        qrCodeRuleP2.setValidDays(dayOfWeeks);
        qrCodeRuleP2.setEnabled(true);

        qrCodeRuleP2.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRuleP2.setValidToDate(LocalDate.of(2018, 1, 31));
        qrCodeRuleP2.setValidFromTime(LocalTime.of(8, 30));
        qrCodeRuleP2.setValidToTime(LocalTime.of(14, 45));

        qrCodeRuleP2.setPriority(2);

        qrCodeRuleRepository.save(qrCodeRuleP2);


        // Miss Rule out of From
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(8,29)));
        ResolveMetaDataDTO redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());

        // Just In rule (from)
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(8,30)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRuleP0.getRedirectURL(), redirectURL.getRedirectURL());

        // Just In rule (to)
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(14,45)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCodeRuleP0.getRedirectURL(), redirectURL.getRedirectURL());

        // Miss Rule out of To
        Mockito.when(timeService.getTime(qrCode.getZoneId())).thenReturn(LocalDateTime.of(LocalDate.of(2018, 1, 1),
                LocalTime.of(14,46)));
        redirectURL = qrCodeRuleResolver.resolveQrCode(qrCode.getId());
        TestCase.assertEquals(qrCode.getDefaultRedirect(), redirectURL.getRedirectURL());
    }
}
