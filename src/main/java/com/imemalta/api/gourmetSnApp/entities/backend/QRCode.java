package com.imemalta.api.gourmetSnApp.entities.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "QRCodes")
@Audited
public class QRCode {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)", unique = true, updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @OneToMany( mappedBy = "qrCode", fetch = FetchType.LAZY, orphanRemoval=true)
    private List<QRCodeRule> qrCodeRules;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name="groupID")
    private Group group;

    @NotNull
    @NotEmpty
    private String defaultRedirect;

    @NotNull
    @NotEmpty
    private String friendlyName;

    private boolean enabled;

    private Long maxBillingCycleScans;

    @JsonIgnore
    @OneToMany( mappedBy = "qrCode", fetch = FetchType.LAZY, orphanRemoval=true)
    @NotAudited
    private List<QRCodeRuleUsage> qrCodeRuleUsage;

    @NotEmpty
    @NotNull
    private String zoneId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<QRCodeRule> getQrCodeRules() {
        return qrCodeRules;
    }

    public void setQrCodeRules(List<QRCodeRule> qrCodeRules) {
        this.qrCodeRules = qrCodeRules;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getDefaultRedirect() {
        return defaultRedirect;
    }

    public void setDefaultRedirect(String defaultRedirect) {
        this.defaultRedirect = defaultRedirect;
    }

    public List<QRCodeRuleUsage> getQrCodeRuleUsage() {
        return qrCodeRuleUsage;
    }

    public void setQrCodeRuleUsage(List<QRCodeRuleUsage> qrCodeRuleUsage) {
        this.qrCodeRuleUsage = qrCodeRuleUsage;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getMaxBillingCycleScans() {
        return maxBillingCycleScans;
    }

    public void setMaxBillingCycleScans(Long maxBillingCycleScans) {
        this.maxBillingCycleScans = maxBillingCycleScans;
    }
}
