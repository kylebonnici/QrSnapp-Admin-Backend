package com.imemalta.api.gourmetSnApp.permissionEvaluators;

import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class QRCodePermissionEvaluators {
    private final QRCodeRepository qrCodeRepository;

    @Autowired
    public QRCodePermissionEvaluators(QRCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    public boolean isOwner(QRCode qrCode) {
        return qrCode.getGroup().getOwner().getUsername().equals(VariantUtils.currentSessionUsername());
    }

    public boolean isOwner(UUID id) {
        Optional<QRCode> establishmentOptional = qrCodeRepository.findById(id);

        return establishmentOptional.filter(this::isOwner).isPresent();

    }
}
