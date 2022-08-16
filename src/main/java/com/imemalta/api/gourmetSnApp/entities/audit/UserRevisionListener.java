package com.imemalta.api.gourmetSnApp.entities.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class UserRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        String username;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        } else {
            username = "Anonymous";
        }

        UserRevEntity exampleRevEntity = (UserRevEntity) revisionEntity;
        exampleRevEntity.setUsername(username);
    }
}