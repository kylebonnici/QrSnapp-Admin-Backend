package com.imemalta.api.gourmetSnApp.entities.audit;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.Entity;

@Entity
@RevisionEntity(UserRevisionListener.class)
class UserRevEntity extends DefaultRevisionEntity {
    private String username;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
