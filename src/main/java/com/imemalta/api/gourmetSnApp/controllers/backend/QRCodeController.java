package com.imemalta.api.gourmetSnApp.controllers.backend;

import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeManagement;
import com.imemalta.api.gourmetSnApp.services.backend.QRCodeRuleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class QRCodeController {
    private final QRCodeManagement qrCodeManagement;
    private final QRCodeRuleResolver qrCodeRuleResolver;

    @Autowired
    public QRCodeController(QRCodeManagement qrCodeManagement, QRCodeRuleResolver qrCodeRuleResolver) {
        this.qrCodeManagement = qrCodeManagement;
        this.qrCodeRuleResolver = qrCodeRuleResolver;
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{id}/count", method = RequestMethod.GET)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP')")
    public long getQrCodeUsageCount(@PathVariable("id") UUID id){
        return qrCodeRuleResolver.qrCodeUsageCount(id);
    }

    @RequestMapping(value = "/profile/groups/{groupId}/qrcodes", method = RequestMethod.GET)
    @PreAuthorize("@groupPermissionEvaluators.isOwner(#groupId) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP')")
    public List<QRCode> getGroupQrCodes(@PathVariable("groupId") long groupId){
        return qrCodeManagement.getGroupsQRCodes(groupId);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{id}", method = RequestMethod.GET)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_VIEW_QRCODES_TO_GROUP')")
    public QRCode get(@PathVariable("id") UUID id){
        return qrCodeManagement.get(id);
    }

    @RequestMapping(value = "/profile/groups/{groupId}/qrcodes/", method = RequestMethod.POST)
    @PreAuthorize("@groupPermissionEvaluators.isOwner(#groupId) || hasRole('ROLE_SUPER_ADMIN_ADD_QRCODES_TO_GROUP')")
    public QRCode add(@PathVariable("groupId") long groupId, @RequestBody QRCode qrCodeDto){
        return qrCodeManagement.add(groupId, qrCodeDto);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{id}", method = RequestMethod.PUT)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_EDIT_QRCODES_TO_GROUP')")
    public QRCode update(@PathVariable("id") UUID id, @RequestBody QRCode qrCodeDto){
        return qrCodeManagement.update(id, qrCodeDto);
    }

    @RequestMapping(value = "/profile/groups/qrcodes/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@QRCodePermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_DELETE_QRCODES_TO_GROUP')")
    public void delete(@PathVariable("id") UUID id) {
         qrCodeManagement.delete(id);
    }

    @RequestMapping(value = "/zoneids/", method = RequestMethod.GET)
    public Set<String> getAllZonIds()
    {
        return ZoneId.getAvailableZoneIds();
    }
}
