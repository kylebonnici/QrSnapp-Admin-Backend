package com.imemalta.api.gourmetSnApp.controllers.authentication;

import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import com.imemalta.api.gourmetSnApp.services.authentication.RoleManagement;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class RoleController {

    private final RoleManagement roleManagement;

    @Autowired
    public RoleController(RoleManagement roleManagement) {
        this.roleManagement = roleManagement;
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/roles", method = RequestMethod.GET)
    public Set<Role> getUserRoles(@PathVariable("username") String username){
        return roleManagement.getUserRolesFromDatabase(username);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/roles", method = RequestMethod.POST)
    public void setUserRoles(@PathVariable("username") String username,
                                               @RequestBody Long[] roleId){
        roleManagement.setUserRoles(username, roleId);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/roles/{roleId}", method = RequestMethod.POST)
    public Role addRoleToUser(@PathVariable("username") String username,
                                                @PathVariable("roleId") Long roleId) {

        return roleManagement.addRolesToUser(username, new Long[] {roleId})[0];
    }


    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/roles/{roleId}", method = RequestMethod.DELETE)
    public void removeRoleFromUser(@PathVariable("username") String username,
                                                     @PathVariable("roleId") Long roleId) {

        roleManagement.removeRolesFromUser(username, new Long[] {roleId});
    }


    @RequestMapping(value = "/user/roles", method = RequestMethod.GET)
    public Set<Role> getSessionUserRoles() {
        return roleManagement.getUserRolesFromDatabase(VariantUtils.currentSessionUsername());
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public List<Role> getRoles() {
        return roleManagement.getRoles();
    }
}
