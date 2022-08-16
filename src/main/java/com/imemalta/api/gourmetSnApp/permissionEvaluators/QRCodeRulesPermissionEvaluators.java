package com.imemalta.api.gourmetSnApp.permissionEvaluators;

import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRuleRepository;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QRCodeRulesPermissionEvaluators {
    private final QRCodeRuleRepository qrCodeRuleRepository;

    @Autowired
    public QRCodeRulesPermissionEvaluators(QRCodeRuleRepository qrCodeRuleRepository) {
        this.qrCodeRuleRepository = qrCodeRuleRepository;
    }

    public boolean isOwner(QRCodeRule qrCodeRule) {
        return qrCodeRule.getQrCode().getGroup().getOwner().getUsername().equals(VariantUtils.currentSessionUsername());
    }

    public boolean isOwner(long id) {
        Optional<QRCodeRule> groupOptional = qrCodeRuleRepository.findById(id);

        return groupOptional.filter(this::isOwner).isPresent();

    }
}
