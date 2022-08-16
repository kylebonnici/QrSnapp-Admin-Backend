package com.imemalta.api.gourmetSnApp.services;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;

import java.util.Optional;

public interface UserService {
    void save(User user);

    Optional<User> findByUsername(String username);
}
