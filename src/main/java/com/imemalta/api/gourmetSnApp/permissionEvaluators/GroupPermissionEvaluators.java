package com.imemalta.api.gourmetSnApp.permissionEvaluators;

import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupPermissionEvaluators {
    private final GroupRepository groupRepository;

    @Autowired
    public GroupPermissionEvaluators(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public boolean isOwner(Group group) {
        return group.getOwner().getUsername().equals(VariantUtils.currentSessionUsername());
    }

    public boolean isOwner(long id) {
        Optional<Group> establishmentOptional = groupRepository.findById(id);

        return establishmentOptional.filter(this::isOwner).isPresent();

    }
}
