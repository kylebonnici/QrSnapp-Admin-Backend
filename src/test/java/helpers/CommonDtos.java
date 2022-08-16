package helpers;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class CommonDtos {
    public static String defaultValidPassword(){
        return "okL3ngth";
    }

    public static User getValidUser(){
        User user = new User();

        user.setName("someName");
        user.setSurname("someSurname");
        user.setUsername("someUsername1");
        user.setEmail("someEmail@email.com");
        user.setAccountState(AccountState.ACTIVE);

        return user;
    }

    public static Group getValidGroup(){
        Group group = new Group();
        group.setName("SomeEstablishment");

        return group;
    }

    public static QRCode getValidQrCode(){
        QRCode qrCode = new QRCode();
        qrCode.setFriendlyName("someFriendlyName");
        qrCode.setDefaultRedirect("https://someteablishmenturl.com");
        qrCode.setZoneId("UTC");
        qrCode.setEnabled(true);

        return qrCode;
    }

    public static QRCodeRule getValidQrCodeRule(){
        QRCodeRule qrCodeRule = new QRCodeRule();

        Set<DayOfWeek> dayOfWeeks = new HashSet<>();
        dayOfWeeks.add(DayOfWeek.MONDAY);

        qrCodeRule.setRedirectURL("SomeUrl");
        qrCodeRule.setFriendlyName("someRuleFriendlyName");
        qrCodeRule.setValidDays(dayOfWeeks);
        qrCodeRule.setValidFromDate(LocalDate.of(2018, 1, 1));
        qrCodeRule.setValidToDate(LocalDate.of(2018, 12, 31));
        qrCodeRule.setValidFromTime(LocalTime.of(0, 0, 0));
        qrCodeRule.setValidToTime(LocalTime.of(23, 59, 59));
        qrCodeRule.setMinCount(0);
        qrCodeRule.setMaxCount(0);
        qrCodeRule.setComparisonType(null);
        qrCodeRule.setPriority(0);

        return qrCodeRule;
    }
}
