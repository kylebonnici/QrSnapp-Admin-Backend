package com.imemalta.api.gourmetSnApp.entities.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imemalta.api.gourmetSnApp.entities.backend.enums.ComparisonType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "QRCodeRules",
        uniqueConstraints = {@UniqueConstraint(columnNames={"id", "priority"})})
@Audited
public class QRCodeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private int priority;

    @JsonIgnore
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name="qrCodeID")
    private QRCode qrCode;

    @NotNull
    @Column(name = "validDays")
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> validDays;

    private LocalDate validFromDate;
    private LocalDate validToDate;

    private LocalTime validFromTime;
    private LocalTime validToTime;

    private Boolean enabled;

    @NotEmpty
    @NotNull
    private String friendlyName;

    @NotEmpty
    @NotNull
    private String redirectURL;

    private Integer minCount, maxCount, countPersistenceDuration;

    @Enumerated(EnumType.STRING)
    private ComparisonType comparisonType;

    @JsonIgnore
    @OneToMany( mappedBy = "qrCodeRule", fetch = FetchType.LAZY, orphanRemoval=true)
    @NotAudited
    private List<QRCodeRuleUsage> qrCodeRuleUsage;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public Set<DayOfWeek> getValidDays() {
        return validDays;
    }

    public void setValidDays(Set<DayOfWeek> validDays) {
        this.validDays = validDays;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public LocalDate getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(LocalDate validFromDate) {
        this.validFromDate = validFromDate;
    }

    public LocalDate getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(LocalDate validToDate) {
        this.validToDate = validToDate;
    }

    public LocalTime getValidFromTime() {
        return validFromTime;
    }

    public void setValidFromTime(LocalTime validFromTime) {
        this.validFromTime = validFromTime;
    }

    public LocalTime getValidToTime() {
        return validToTime;
    }

    public void setValidToTime(LocalTime validToTime) {
        this.validToTime = validToTime;
    }

    public List<QRCodeRuleUsage> getQrCodeRuleUsage() {
        return qrCodeRuleUsage;
    }

    public void setQrCodeRuleUsage(List<QRCodeRuleUsage> qrCodeRuleUsage) {
        this.qrCodeRuleUsage = qrCodeRuleUsage;
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(ComparisonType comparisonType) {
        this.comparisonType = comparisonType;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public Integer getCountPersistenceDuration() {
        return countPersistenceDuration;
    }

    public void setCountPersistenceDuration(Integer countPersistenceDuration) {
        this.countPersistenceDuration = countPersistenceDuration;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
