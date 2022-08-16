package com.imemalta.api.gourmetSnApp.entities.backend.repositories;


import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.backend.UserMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMetadataRepository extends JpaRepository<UserMetadata, UUID> {
    Optional<UserMetadata> findByCustomerID(String customerId);
    Optional<UserMetadata> findByUser(User user);
    Optional<UserMetadata> findByUserUsername(String username);
    Optional<UserMetadata> findByUserId(long userId);
}
