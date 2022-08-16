package com.imemalta.api.gourmetSnApp.entities.authentication.repositories;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
