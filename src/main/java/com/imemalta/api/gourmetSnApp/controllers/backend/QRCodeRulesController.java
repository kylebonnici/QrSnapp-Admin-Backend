package com.imemalta.api.gourmetSnApp.controllers.backend;

import com.imemalta.api.gourmetSnApp.dtos.ResolveMetaDataDTO;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCodeRule;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleManagement;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class QRCodeRulesController {
    private final QRCodeRuleManagement qrCodeRuleManagement;
    private final QRCodeRuleResolver qrCodeRuleResolver;

    @Autowired
    public QRCodeRulesController(QRCodeRuleManagement qrCodeRuleManagement, QRCodeRuleResolver qrCodeRuleResolver) {
        this.qrCodeRuleManagement = qrCodeRuleManagement;
        this.qrCodeRuleResolver = qrCodeRuleResolver;
    }

    @RequestMapping(value = "/public/resolve/qrcodes/{qrCodeId}", method = RequestMethod.GET)
    public ResolveMetaDataDTO resolve(@PathVariable("qrCodeId") String qrCodeId){
        return qrCodeRuleResolver.resolveQrCode(UUID.fromString(qrCodeId));
    }

    @RequestMapping(value = "/profile/groups/qrcodes/rules/{id}/count", method = RequestMethod.GET)
    @PreAuthorize("@QRCodeRulesPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE')")
    public long getRuleUsageCount(@PathVariable("id") long id){
        return qrCodeRuleResolver.qrCodeRuleUsageCount(id);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/rules/{id}", method = RequestMethod.GET)
    @PreAuthorize("@QRCodeRulesPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE')")
    public QRCodeRule get(@PathVariable("id") long id){
        return qrCodeRuleManagement.get(id);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{qrCodeId}/rules", method = RequestMethod.GET)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#qrCodeId) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODE_RULE')")
    public List<QRCodeRule> getQrCodeRules(@PathVariable("qrCodeId") UUID qrCodeId){
        return qrCodeRuleManagement.getQRCodeRules(qrCodeId);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{qrCodeId}/rules/", method = RequestMethod.POST)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#qrCodeId) || hasRole('ROLE_SUPER_ADMIN_ADD_QRCODE_RULE')")
    public QRCodeRule add(@PathVariable("qrCodeId") UUID qrCodeId, @RequestBody QRCodeRule qrCodeRule){
        return qrCodeRuleManagement.add(qrCodeRule, qrCodeId);
    }

    @PreAuthorize("@QRCodeRulesPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_EDIT_QRCODE_RULE')")
    @RequestMapping(value = "/profile/groups/qrcodes/rules/{id}", method = RequestMethod.PUT)
    public QRCodeRule update(@PathVariable("id") long id, @RequestBody QRCodeRule qrCodeRule) {
        return qrCodeRuleManagement.update(id, qrCodeRule);
    }

    @PreAuthorize("@QRCodeRulesPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_DELETE_QRCODE_RULE')")
    @RequestMapping(value = "/profile/groups/qrcodes/rules/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") long id) {
        qrCodeRuleManagement.delete(id);
    }
}
