package com.imemalta.api.gourmetSnApp.services.backend;

import com.imemalta.api.gourmetSnApp.dtos.ApiError;
import com.imemalta.api.gourmetSnApp.dtos.ApiErrorCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QRCodeRuleManagement {
    private final QRCodeRuleRepository qrCodeRuleRepository;
    private final QRCodeRepository qrCodeRepository;

    @Autowired
    public QRCodeRuleManagement(QRCodeRuleRepository qrCodeRuleRepository, QRCodeRepository qrCodeRepository){
        this.qrCodeRuleRepository = qrCodeRuleRepository;
        this.qrCodeRepository = qrCodeRepository;
    }

    @Transactional(readOnly = true)
    public QRCodeRule get(long id) {
        Optional<QRCodeRule> qrCodeRule = qrCodeRuleRepository.findById(id);

        if (qrCodeRule.isEmpty()){
            throw new EntityNotFoundException("qrCodeRule", id);
        }

        return qrCodeRule.get();
    }

    @Transactional(readOnly = true)
    public List<QRCodeRule> getQRCodeRules(UUID qrCodeId) {
        Optional<QRCode> qrCode = qrCodeRepository.findById(qrCodeId);

        if (qrCode.isEmpty()){
            throw new EntityNotFoundException("qrCode", qrCodeId.toString());
        }

        return qrCodeRuleRepository.findByQrCode(qrCode.get());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public QRCodeRule add(QRCodeRule qrCodeRuleDto, UUID qrCodeId){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(qrCodeRuleDto, new String[] {"qrCode"}));

        Optional<QRCode> qrCodeOptional = qrCodeRepository.findById(qrCodeId);

        if (qrCodeOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCode", qrCodeId.toString());
        }

        if (qrCodeRuleRepository.findByQrCodeAndPriority(qrCodeOptional.get(), qrCodeRuleDto.getPriority()).size() != 0) {
            throw new ConstraintViolationException(new ApiError(ApiErrorCode.INDEX_ALREADY_IN_USE, "QrCode already has another rule with the same priority"));
        }

        QRCodeRule qrCodeRule = new QRCodeRule();
        qrCodeRule.setEnabled(qrCodeRuleDto.getEnabled());
        qrCodeRule.setValidFromDate(qrCodeRuleDto.getValidFromDate());
        qrCodeRule.setValidToDate(qrCodeRuleDto.getValidToDate());
        qrCodeRule.setValidToTime(qrCodeRuleDto.getValidToTime());
        qrCodeRule.setValidFromTime(qrCodeRuleDto.getValidFromTime());
        qrCodeRule.setRedirectURL(qrCodeRuleDto.getRedirectURL());
        qrCodeRule.setFriendlyName(qrCodeRuleDto.getFriendlyName());
        qrCodeRule.setPriority(qrCodeRuleDto.getPriority());
        qrCodeRule.setQrCode(qrCodeOptional.get());
        qrCodeRule.setMinCount(qrCodeRuleDto.getMinCount());
        qrCodeRule.setMaxCount(qrCodeRuleDto.getMaxCount());
        qrCodeRule.setComparisonType(qrCodeRuleDto.getComparisonType());
        qrCodeRule.setValidDays(qrCodeRuleDto.getValidDays());
        qrCodeRule.setCountPersistenceDuration(qrCodeRuleDto.getCountPersistenceDuration());

        // TODO limit no of establishments on subscription type

        qrCodeRuleRepository.save(qrCodeRule);

        return qrCodeRule;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(long id) {
        Optional<QRCodeRule> qrCodeRule = qrCodeRuleRepository.findById(id);

        if (qrCodeRule.isEmpty()) {
            throw new EntityNotFoundException("qrCodeRule", id);
        }

        qrCodeRuleRepository.delete(qrCodeRule.get());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public  QRCodeRule update(long id, QRCodeRule qrCodeRuleDto){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(qrCodeRuleDto, new String[] {"qrCode"}));

        Optional<QRCodeRule> qrCodeRuleOptional = qrCodeRuleRepository.findById(id);

        if (qrCodeRuleOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCodeRule", id);
        }

        List<QRCodeRule> rulesWithSamePriority = qrCodeRuleRepository.findByQrCodeAndPriority(qrCodeRuleOptional.get().getQrCode(), qrCodeRuleDto.getPriority());
        rulesWithSamePriority.forEach((rule) -> {
            if (rule.getId() != id) {
                throw new ConstraintViolationException(new ApiError(ApiErrorCode.INDEX_ALREADY_IN_USE, "QrCode already has another rule with the same priority"));
            }
        });

        QRCodeRule qrCodeRule = qrCodeRuleOptional.get();
        qrCodeRule.setEnabled(qrCodeRuleDto.getEnabled());
        qrCodeRule.setValidFromDate(qrCodeRuleDto.getValidFromDate());
        qrCodeRule.setValidToDate(qrCodeRuleDto.getValidToDate());
        qrCodeRule.setValidToTime(qrCodeRuleDto.getValidToTime());
        qrCodeRule.setValidFromTime(qrCodeRuleDto.getValidFromTime());
        qrCodeRule.setRedirectURL(qrCodeRuleDto.getRedirectURL());
        qrCodeRule.setFriendlyName(qrCodeRuleDto.getFriendlyName());
        qrCodeRule.setPriority(qrCodeRuleDto.getPriority());
        qrCodeRule.setValidDays(qrCodeRuleDto.getValidDays());
        qrCodeRule.setMinCount(qrCodeRuleDto.getMinCount());
        qrCodeRule.setMaxCount(qrCodeRuleDto.getMaxCount());
        qrCodeRule.setComparisonType(qrCodeRuleDto.getComparisonType());
        qrCodeRule.setCountPersistenceDuration(qrCodeRuleDto.getCountPersistenceDuration());

        qrCodeRuleRepository.save(qrCodeRule);

        return qrCodeRule;
    }
}
