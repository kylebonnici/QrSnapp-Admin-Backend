package com.imemalta.api.gourmetSnApp.services.backend;

import com.imemalta.api.gourmetSnApp.dtos.ResolveMetaDataDTO;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRuleUsage;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleUsageRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.exceptions.common.UnpaidSubscriptionException;
import com.imemalta.api.gourmetSnApp.services.TimeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class QRCodeRuleResolver {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final TimeService timeService;
    private final QRCodeRuleRepository qrCodeRuleRepository;
    private final QRCodeRepository qrCodeRepository;
    private final QRCodeRuleUsageRepository qrCodeRuleUsageRepository;
    private final StripeSubscriptionManagement stripeSubscriptionManagement;

    @Autowired
    public QRCodeRuleResolver(TimeService timeService, QRCodeRuleRepository qrCodeRuleRepository,
                              QRCodeRepository qrCodeRepository,
                              QRCodeRuleUsageRepository qrCodeRuleUsageRepository, StripeSubscriptionManagement stripeSubscriptionManagement) {
        this.timeService = timeService;
        this.qrCodeRuleRepository = qrCodeRuleRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.qrCodeRuleUsageRepository = qrCodeRuleUsageRepository;
        this.stripeSubscriptionManagement = stripeSubscriptionManagement;
    }

    @Transactional(readOnly = true)
    public long qrCodeRuleUsageCount(long qrCodeRuleId) {
        Optional<QRCodeRule> qrCodeRuleOptional = qrCodeRuleRepository.findById(qrCodeRuleId);

        if (qrCodeRuleOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCodeRule", qrCodeRuleId);
        }

        return qrCodeRuleUsageRepository.countByQrCodeRule(qrCodeRuleOptional.get());
    }

    @Transactional(readOnly = true)
    public long qrCodeRuleUsageCount(long qrCodeRuleId, LocalDateTime from, LocalDateTime to) {
        Optional<QRCodeRule> qrCodeRuleOptional = qrCodeRuleRepository.findById(qrCodeRuleId);

        if (qrCodeRuleOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCodeRule", qrCodeRuleId);
        }

        return qrCodeRuleUsageRepository.countByQrCodeRuleAndTimestampBetween(qrCodeRuleOptional.get(), from, to);
    }

    @Transactional(readOnly = true)
    public long qrCodeUsageCount(UUID qrCodeId) {
        Optional<QRCode> qrCodeOptional = qrCodeRepository.findById(qrCodeId);

        if (qrCodeOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCode", qrCodeId.toString());
        }

        return qrCodeRuleUsageRepository.countByQrCode(qrCodeOptional.get());
    }

    @Transactional(readOnly = true)
    public long qrCodeUsageCount(UUID qrCodeId, LocalDateTime from, LocalDateTime to) {
        Optional<QRCode> qrCodeOptional = qrCodeRepository.findById(qrCodeId);

        if (qrCodeOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCode", qrCodeId.toString());
        }

        return qrCodeRuleUsageRepository.countByQrCodeAndTimestampBetween(qrCodeOptional.get(), from, to);
    }

    public ResolveMetaDataDTO resolveQrCode(UUID id) {
        ResolveMetaDataDTO out = new ResolveMetaDataDTO();

        Optional<QRCode> qrCode = qrCodeRepository.findById(id);

        if (qrCode.isPresent()) {

            if (!qrCode.get().getEnabled()) {
                throw new EntityNotFoundException("qrCode", id.toString());
            }

            UserMetadata userMetadata = qrCode.get().getGroup().getOwner().getUserMetadata();

            if (!userMetadata.isQrCodeSubscriptionActive()) {
                throw new UnpaidSubscriptionException();
            }

            long scansThisBillingCycle = qrCodeRuleUsageRepository.countByQrCodeAndReportedInSubscriptionId(qrCode.get(),
                    userMetadata.getQrCodeSubscriptionId());

            long unreportedScansThisBillingCycle = qrCodeRuleUsageRepository.countByQrCodeAndReportedInSubscriptionId(qrCode.get(),
                    null);

            if (qrCode.get().getMaxBillingCycleScans() != null && (scansThisBillingCycle + unreportedScansThisBillingCycle) >= qrCode.get().getMaxBillingCycleScans()) {
                throw new EntityNotFoundException("qrCode", id.toString()); // TODO no longer valid
            }

            List<QRCodeRule> qrCodeRules = qrCodeRuleRepository.findByQrCode(qrCode.get());
            qrCodeRules.sort(Comparator.comparingInt(QRCodeRule::getPriority));

            for (QRCodeRule rule : qrCodeRules) {
                if (computeRuleValue(rule)) {
                    QRCodeRuleUsage qrCodeRuleUsage = new QRCodeRuleUsage();
                    qrCodeRuleUsage.setQrCodeRule(rule);
                    qrCodeRuleUsage.setQrCode(qrCode.get());
                    qrCodeRuleUsage.setTimestamp(timeService.getTime());
                    qrCodeRuleUsageRepository.save(qrCodeRuleUsage);

                    out.setRedirectURL(rule.getRedirectURL());

                    executorService.schedule(stripeSubscriptionManagement::updateUnmonitoredQrcodeUsage
                    , 1, TimeUnit.SECONDS);

                    return out;
                }
            }

            QRCodeRuleUsage qrCodeRuleUsage = new QRCodeRuleUsage();
            qrCodeRuleUsage.setQrCode(qrCode.get());
            qrCodeRuleUsage.setTimestamp(timeService.getTime());
            qrCodeRuleUsageRepository.save(qrCodeRuleUsage);

            out.setRedirectURL(qrCode.get().getDefaultRedirect());


            executorService.schedule(stripeSubscriptionManagement::updateUnmonitoredQrcodeUsage, 1, TimeUnit.SECONDS);

            return out;
        }

        throw new EntityNotFoundException("qrCode", id.toString());
    }

    private boolean computeRuleValue(QRCodeRule qrCodeRule) {
        if (!qrCodeRule.getEnabled()) {
            return false;
        }

        LocalDate nowDate = timeService.getTime(qrCodeRule.getQrCode().getZoneId()).toLocalDate();

        if (qrCodeRule.getValidFromDate() != null && nowDate.isBefore(qrCodeRule.getValidFromDate()))
            return false;

        if (qrCodeRule.getValidToDate() != null && nowDate.isAfter(qrCodeRule.getValidToDate()))
            return false;

        if (qrCodeRule.getValidDays().size() != 0 && !qrCodeRule.getValidDays().contains(nowDate.getDayOfWeek()))
            return false;


        LocalTime nowTime = timeService.getTime(qrCodeRule.getQrCode().getZoneId()).toLocalTime();

        if (qrCodeRule.getValidFromTime() != null && nowTime.isBefore(qrCodeRule.getValidFromTime().withSecond(0)))
            return false;

        if (qrCodeRule.getValidToTime() != null && nowTime.isAfter(qrCodeRule.getValidToTime().withSecond(59)))
            return false;

        if (qrCodeRule.getComparisonType() == null)
            return true;

        long count;

        if (qrCodeRule.getCountPersistenceDuration() != null) {
            LocalDateTime now = timeService.getTime();
            LocalDateTime past = now.minus(qrCodeRule.getCountPersistenceDuration(), ChronoUnit.HOURS);

            count = qrCodeRuleUsageRepository.countByQrCodeAndTimestampBetween(qrCodeRule.getQrCode(), past, now);
        } else {
            count = qrCodeRuleUsageRepository.countByQrCode(qrCodeRule.getQrCode());
        }

        count ++; // consider this scan

        switch (qrCodeRule.getComparisonType()) {
            case BETWEEN:
                return count >= qrCodeRule.getMinCount() && count <= qrCodeRule.getMaxCount();
            case EXCLUSION:
                return count < qrCodeRule.getMinCount() || count > qrCodeRule.getMaxCount();
            case EQUAL:
                return count == qrCodeRule.getMinCount();
            case GREATER:
                return count > qrCodeRule.getMinCount();
            case LESS:
                return count < qrCodeRule.getMinCount();
            default:
                return true;
        }

    }
}
