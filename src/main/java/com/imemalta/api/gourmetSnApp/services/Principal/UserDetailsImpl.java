package com.imemalta.api.gourmetSnApp.services.Principal;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.enums.AccountState;
import com.imemalta.api.gourmetSnApp.services.TimeService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

public class UserDetailsImpl implements UserDetails {
    private final TimeService timeService;
    private final User user;
    private final Set<GrantedAuthority> grantedAuthorities;

    public UserDetailsImpl(User user, Set<GrantedAuthority> grantedAuthorities, TimeService timeService) {
        this.user = user;
        this.grantedAuthorities = grantedAuthorities;
        this.timeService = timeService;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getUserMetadata().getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        LocalDateTime currentDate = timeService.getTime();

        if (user.getValidFrom() != null && user.getValidTo() != null) {
            return !currentDate.isBefore(user.getValidFrom()) &&
                    !currentDate.isAfter(user.getValidTo());
        } else if (user.getValidFrom() != null) {
            return !currentDate.isBefore(user.getValidFrom());
        } else if (user.getValidTo() != null) {
            return !currentDate.isAfter(user.getValidTo());
        } else {
            return true;
        }
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountState() != AccountState.LOCKED &&
                user.getAccountState() != AccountState.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getAccountState() != AccountState.PENDING_ACTIVATION &&
                user.getAccountState() != AccountState.PENDING_PASSWORD &&
                !user.getUserMetadata().isPasswordExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getAccountState() == AccountState.ACTIVE;
    }

    @Override
    public boolean equals(Object otherUser) {
        if(otherUser == null) return false;
        else if (!(otherUser instanceof UserDetails)) return false;
        else return (otherUser.hashCode() == hashCode());
    }

    @Override
    public int hashCode() {
        return user.getUsername().hashCode() ;
    }
}
