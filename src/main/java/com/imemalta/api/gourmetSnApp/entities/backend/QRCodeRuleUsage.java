package com.imemalta.api.gourmetSnApp.entities.backend;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "QRCodeRulesUsage")
public class QRCodeRuleUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name="qrCodeRule")
    private QRCodeRule qrCodeRule;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name="qrCode")
    private QRCode qrCode;

    private boolean reportedUsage;
    private String reportedInSubscriptionId;

    private LocalDateTime timestamp;

    public QRCodeRule getQrCodeRule() {
        return qrCodeRule;
    }

    public void setQrCodeRule(QRCodeRule qrCodeRule) {
        this.qrCodeRule = qrCodeRule;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public QRCode getQrCode() {
        return qrCode;
    }

    public void setQrCode(QRCode qrCode) {
        this.qrCode = qrCode;
    }

    public boolean isReportedUsage() {
        return reportedUsage;
    }

    public void setReportedUsage(boolean reportedUsage) {
        this.reportedUsage = reportedUsage;
    }

    public String getReportedInSubscriptionId() {
        return reportedInSubscriptionId;
    }

    public void setReportedInSubscriptionId(String reportedInSubscriptionId) {
        this.reportedInSubscriptionId = reportedInSubscriptionId;
    }
}
