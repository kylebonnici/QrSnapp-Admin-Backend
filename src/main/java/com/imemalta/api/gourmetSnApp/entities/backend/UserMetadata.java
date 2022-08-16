package com.imemalta.api.gourmetSnApp.entities.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "UsersMetadata")
@Audited
public class UserMetadata implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotAudited
    private String customerID;

    @NotAudited
    private String qrCodeSubscriptionId;

    @NotAudited
    private boolean qrCodeSubscriptionActive;

    @NotAudited
    private Long qrCodeSubscriptionStartCurrentPeriod;

    @NotAudited
    private Long qrCodeSubscriptionEndCurrentPeriod;

    @JsonIgnore
    @NotNull
    @OneToOne(mappedBy = "userMetadata")
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @NotAudited
    private List<String> reportedQrCodes = new ArrayList<String>();

    @JsonIgnore
    private boolean passwordExpired;


    private String password;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQrCodeSubscriptionId() {
        return qrCodeSubscriptionId;
    }

    public void setQrCodeSubscriptionId(String qrCodeSubscriptionId) {
        this.qrCodeSubscriptionId = qrCodeSubscriptionId;
    }

    public List<String> getReportedQrCodes() {
        return reportedQrCodes;
    }

    public void setReportedQrCodes(List<String> reportedQrCodes) {
        this.reportedQrCodes = reportedQrCodes;
    }

    public Long getQrCodeSubscriptionEndCurrentPeriod() {
        return qrCodeSubscriptionEndCurrentPeriod;
    }

    public void setQrCodeSubscriptionEndCurrentPeriod(Long qrCodeSubscriptionEndCurrentPeriod) {
        this.qrCodeSubscriptionEndCurrentPeriod = qrCodeSubscriptionEndCurrentPeriod;
    }

    public Long getQrCodeSubscriptionStartCurrentPeriod() {
        return qrCodeSubscriptionStartCurrentPeriod;
    }

    public void setQrCodeSubscriptionStartCurrentPeriod(Long qrCodeSubscriptionStartCurrentPeriod) {
        this.qrCodeSubscriptionStartCurrentPeriod = qrCodeSubscriptionStartCurrentPeriod;
    }

    public boolean isQrCodeSubscriptionActive() {
        return qrCodeSubscriptionActive;
    }

    public void setQrCodeSubscriptionActive(boolean qrCodeSubscriptionActive) {
        this.qrCodeSubscriptionActive = qrCodeSubscriptionActive;
    }
}
