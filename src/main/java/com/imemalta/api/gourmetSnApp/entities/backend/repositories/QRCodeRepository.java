package com.imemalta.api.gourmetSnApp.entities.backend.repositories;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.entities.backend.Group;
import com.imemalta.api.gourmetSnApp.entities.backend.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QRCodeRepository extends JpaRepository<QRCode, UUID> {
    Optional<QRCode> findById(UUID id);
    List<QRCode> findByGroup(Group group);
    List<QRCode> findByGroupOwner(User user);
    int countByGroupOwner(User user);
}
