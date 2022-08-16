package com.imemalta.api.gourmetSnApp.entities.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "Groups")
@Audited
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name="userID")
    private User owner;

    @NotNull
    @NotEmpty
    private String name;

    @JsonIgnore
    @OneToMany( mappedBy = "group", fetch = FetchType.LAZY, orphanRemoval=true)
    private List<QRCode> qrCodes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QRCode> getQrCodes() {
        return qrCodes;
    }

    public void setQrCodes(List<QRCode> qrCodes) {
        this.qrCodes = qrCodes;
    }
}
