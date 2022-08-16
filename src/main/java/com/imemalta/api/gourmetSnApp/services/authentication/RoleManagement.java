package com.imemalta.api.gourmetSnApp.services.authentication;

import com.imemalta.api.gourmetSnApp.exceptions.authentication.ChangeOwnUserRolesException;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.UserNotFoundException;
import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.RoleRepository;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.utils.SessionUtils;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SuppressWarnings("DefaultAnnotationParam")
@Service
public class RoleManagement {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionRegistry sessionRegistry;

    @Autowired
    public RoleManagement(UserRepository userRepository, RoleRepository roleRepository, SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sessionRegistry = sessionRegistry;
    }

    @Transactional(readOnly = true)
    public Set<Role> getUserRolesFromDatabase(String username) {
        return roleRepository.findByUsers_username(username);
    }

    public void setUserRoles(String username, Long[] roleId){
        if (username.equals(VariantUtils.currentSessionUsername())){
            throw new ChangeOwnUserRolesException();
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        Set<Role> roles = roleRepository.findByIdIn(roleId);

        user.get().setRoles(roles);
        userRepository.save(user.get());

        SessionUtils.expireUserSessions(user.get().getUsername(), sessionRegistry);
    }

    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public Role[] addRolesToUser(String username,
                                                 Long[] roleIds){
        if (username.equals(VariantUtils.currentSessionUsername())){
            throw  new ChangeOwnUserRolesException();
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        Set<Role> roles = roleRepository.findByIdIn(roleIds);

        if (roles.size() != roleIds.length) {
            List<Long> missingIds = Arrays.asList(roleIds);
            for (Role role: roles) {
                missingIds.remove(role.getId());
            }
            throw new EntityNotFoundException("role", missingIds);
        }

        if (user.get().getRoles() == null) {
            user.get().setRoles(roles);
        } else {
            user.get().getRoles().addAll(roles);
        }

        //userRepository.save(user.get());

        SessionUtils.expireUserSessions(user.get().getUsername(), sessionRegistry);

        Role[] rolesOut = new Role[roles.size()];
        int i = 0;
        for (Role role: roles) {
            rolesOut[i++] = role;
        }

        return rolesOut;
    }

    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public void removeRolesFromUser(String username, Long[] roleIds){
        if (username.equals(VariantUtils.currentSessionUsername())){
            throw new ChangeOwnUserRolesException();
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserNotFoundException();
        }

        Set<Role> roles = roleRepository.findByIdIn(roleIds);
        if (user.get().getRoles() != null) {
            user.get().getRoles().removeAll(roles);

            if (user.get().getRoles().size() == 0) {
                user.get().setRoles(null);
            }
        }

        userRepository.save(user.get());
        SessionUtils.expireUserSessions(user.get().getUsername(), sessionRegistry);
    }

    @Transactional(readOnly = true)
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }
}
