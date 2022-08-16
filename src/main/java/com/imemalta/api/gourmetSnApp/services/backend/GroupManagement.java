package com.imemalta.api.gourmetSnApp.services.backend;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.authentication.repositories.UserRepository;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.repositories.GroupRepository;
import com.imemalta.api.gourmetSnApp.exceptions.authentication.UserNotFoundException;
import com.imemalta.api.gourmetSnApp.exceptions.common.EntityNotFoundException;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("DefaultAnnotationParam") 
@Service
public class GroupManagement {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupManagement(GroupRepository groupRepository, UserRepository userRepository){
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Group> getAll() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Group> getOwnersGroups(String username) {
        Optional<User> groupOwner = userRepository.findByUsername(username);

        if (groupOwner.isEmpty()) {
            throw new UserNotFoundException();
        }

        List<Group> groups = groupRepository.findByOwner(groupOwner.get());
        return groups == null ? new ArrayList<>() : groups;
    }

    @Transactional(readOnly = true)
    public Group get(long id) {
        Optional<Group> group = groupRepository.findById(id);

        if (group.isEmpty()) {
            throw new EntityNotFoundException("group", id);
        }

        return group.get();
    }

    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public Group add(Group groupDto, String establishmentOwnerUserName){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(groupDto, new String[] {"owner"}));

        Optional<User> establishmentOwner = userRepository.findByUsername(establishmentOwnerUserName);

        if (establishmentOwner.isEmpty()) {
            throw new UserNotFoundException();
        }

        Group group = new Group();
        group.setName(groupDto.getName());
        group.setOwner(establishmentOwner.get());

        groupRepository.save(group);

        return group;
    }

    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public void changeOwner(long id, String newEstablishmentOwnerUserName){
        Optional<User> groupOwner = userRepository.findByUsername(newEstablishmentOwnerUserName);
        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOwner.isEmpty()) {
            throw new UserNotFoundException();
        }

        if (groupOptional.isEmpty()) {
            throw new EntityNotFoundException("group", id );
        }

        groupOptional.get().setOwner(groupOwner.get());

        groupRepository.save(groupOptional.get());
    }


    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public Group update(long id, Group groupDto){
        VariantUtils.throwConstraintViolations(VariantUtils.validateConstraints(groupDto, new String[] {"owner"}));

        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isEmpty()) {
            throw new EntityNotFoundException("group", id);
        }

        Group group = groupOptional.get();
        group.setName(groupDto.getName());

        groupRepository.save(group);

        return group;
    }

    @Transactional(readOnly = false, propagation = Propagation.SUPPORTS)
    public void delete(long id) {
        Optional<Group> groupOptional = groupRepository.findById(id);

        if (groupOptional.isEmpty()) {
            throw new EntityNotFoundException("group", id);
        }

        groupRepository.delete(groupOptional.get());
    }
}
