package com.imemalta.api.gourmetSnApp.entities.backend.repositories;

import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRuleUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface QRCodeRuleUsageRepository extends JpaRepository<QRCodeRuleUsage, Long> {
    long countByQrCodeRule(QRCodeRule qrCodeRule);
    long countByQrCode(QRCode qrCode);
    long countByQrCodeAndReportedInSubscriptionId(QRCode qrCode, String subscriptionId);
    long countByQrCodeIdAndReportedInSubscriptionId(UUID qrCodeId, String subscriptionId);
    long countByQrCodeId(UUID qrCodeId);
    long countByReportedInSubscriptionId(String subscriptionId);

    @Query("select count(distinct ruleUsage.qrCode) from QRCodeRuleUsage ruleUsage where ruleUsage.qrCode.group.owner.username = :username and ruleUsage.reportedInSubscriptionId = :subscriptionId")
    long countSubscriptionUniqueQrCodeUsage(String username, String subscriptionId);

    long countByQrCodeRuleAndTimestampBetween(QRCodeRule qrCodeRule, LocalDateTime from, LocalDateTime to);
    long countByQrCodeAndTimestampBetween(QRCode qrCode, LocalDateTime from, LocalDateTime to);
    List<QRCodeRuleUsage> findByReportedUsage(boolean reportedUsage);
}
