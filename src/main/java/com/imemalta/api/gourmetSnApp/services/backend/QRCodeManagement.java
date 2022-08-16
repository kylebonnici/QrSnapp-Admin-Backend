package com.imemalta.api.gourmetSnApp.services.backend;

import com.imemalta.api.gourmetSnApp.dtos.ApiError;
import com.imemalta.api.gourmetSnApp.dtos.ApiErrorCode;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.QRCodeRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.ConstraintViolationException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;

@Service
public class QRCodeManagement {
    private final QRCodeRepository qrCodeRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public QRCodeManagement(QRCodeRepository qrCodeRepository, GroupRepository groupRepository) {
        this.qrCodeRepository = qrCodeRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public QRCode get(UUID id) {
        Optional<QRCode> qrCode = qrCodeRepository.findById(id);

        if (qrCode.isEmpty()) {
            throw new EntityNotFoundException("qrCode", id.toString());
        }

        return qrCode.get();
    }

    @Transactional(readOnly = true)
    public List<QRCode> getGroupsQRCodes(long groupId) {
        Optional<Group> group = groupRepository.findById(groupId);

        if (group.isEmpty()) {
            throw new EntityNotFoundException("group", groupId);
        }

        return qrCodeRepository.findByGroup(group.get());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public QRCode add(long groupId, QRCode qrCodeDto) {
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(qrCodeDto, new String[] {"establishment"}));

        Optional<Group> group = groupRepository.findById(groupId);

        if (group.isEmpty()) {
            throw new EntityNotFoundException("group", groupId);
        }

        QRCode qrCode = new QRCode();
        qrCode.setGroup(group.get());
        qrCode.setFriendlyName(qrCodeDto.getFriendlyName());
        qrCode.setDefaultRedirect(qrCodeDto.getDefaultRedirect());
        qrCode.setEnabled(qrCodeDto.getEnabled());
        qrCode.setZoneId(qrCodeDto.getZoneId());
        qrCode.setMaxBillingCycleScans(qrCodeDto.getMaxBillingCycleScans());

        if (!ZoneId.getAvailableZoneIds().contains(qrCodeDto.getZoneId())) {
            throw new ConstraintViolationException(new ApiError(ApiErrorCode.CONSTRAIN_VIOLATION, "Invalid Zone ID"));
        }

        qrCodeRepository.save(qrCode);

        return qrCode;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public QRCode update(UUID qrCodeId, QRCode qrCodeDto){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(qrCodeDto, new String[] {"establishment"}));

        Optional<QRCode> qrCodeOptional = qrCodeRepository.findById(qrCodeId);

        if (qrCodeOptional.isEmpty()) {
            throw new EntityNotFoundException("qrCode", qrCodeId.toString());
        }

        qrCodeOptional.get().setFriendlyName(qrCodeDto.getFriendlyName());
        qrCodeOptional.get().setDefaultRedirect(qrCodeDto.getDefaultRedirect());
        qrCodeOptional.get().setZoneId(qrCodeDto.getZoneId());
        qrCodeOptional.get().setEnabled(qrCodeDto.getEnabled());
        qrCodeOptional.get().setMaxBillingCycleScans(qrCodeDto.getMaxBillingCycleScans());

        if (!ZoneId.getAvailableZoneIds().contains(qrCodeDto.getZoneId())) {
            throw new ConstraintViolationException(new ApiError(ApiErrorCode.CONSTRAIN_VIOLATION, "Invalid Zone ID"));
        }

        qrCodeRepository.save(qrCodeOptional.get());

        return qrCodeOptional.get();
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(UUID id) {
        Optional<QRCode> qrCode = qrCodeRepository.findById(id);

        if (qrCode.isEmpty()) {
            throw new EntityNotFoundException("qrCode", id.toString());
        }

        qrCodeRepository.delete(qrCode.get());
    }

}
