package com.imemalta.api.gourmetSnApp.controllers.backend;

import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.services.backend.GroupManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class GroupController {
    private final GroupManagement groupManagement;

    @Autowired
    public GroupController(GroupManagement groupManagement) {
        this.groupManagement = groupManagement;
    }

    @RequestMapping(value = "/profile/groups/{id}", method = RequestMethod.GET)
    public Group get(@PathVariable("id") long id){
        return groupManagement.get(id);
    }

    @RequestMapping(value = "/profile/{username}/groups/", method = RequestMethod.GET)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_VIEW_OWNERS_GROUP')")
    public List<Group> getOwnersGroup(@PathVariable("username") String username){
        return groupManagement.getOwnersGroups(username);
    }

    @RequestMapping(value = "/profile/{username}/groups/", method = RequestMethod.POST)
    @PreAuthorize("@identityPermissionsEvaluators.isLoggedInUser(#username) || hasRole('ROLE_SUPER_ADMIN_ADD_GROUP')")
    public Group add(@RequestBody Group group, @PathVariable("username") String username){
        return groupManagement.add(group, username);
    }

    @RequestMapping(value = "/profile/groups/{id}", method = RequestMethod.PUT)
    @PreAuthorize("@groupPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_EDIT_GROUP')")
    public Group update(@PathVariable("id") long id, @RequestBody Group group) {
        return groupManagement.update(id, group);
    }

    @RequestMapping(value = "/profile/groups/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@groupPermissionEvaluators.isOwner(#id) || hasRole('ROLE_SUPER_ADMIN_DELETE_GROUP')")
    public void delete(@PathVariable("id") long id) {
        groupManagement.delete(id);
    }
}
