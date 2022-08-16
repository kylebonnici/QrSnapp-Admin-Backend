package com.imemalta.api.gourmetSnApp.entities.authentication.repositories;

import com.imemalta.api.gourmetSnApp.entities.authentication.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Set<Role> findByIdIn(Long[] ids);
    Set<Role> findByNameIn(String[] names);
    Set<Role> findByUsers_username(String username);
}