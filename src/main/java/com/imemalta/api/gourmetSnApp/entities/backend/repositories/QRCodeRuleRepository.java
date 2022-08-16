package com.imemalta.api.gourmetSnApp.entities.backend.repositories;

import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QRCodeRuleRepository extends JpaRepository<QRCodeRule, Long> {
    Optional<QRCodeRule> findById(Long id);
    List<QRCodeRule> findByQrCode(QRCode qrCode);
    List<QRCodeRule> findByQrCodeAndPriority(QRCode qrCode, int id);
}
