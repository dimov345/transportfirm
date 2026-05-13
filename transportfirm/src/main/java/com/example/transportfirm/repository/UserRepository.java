package com.example.transportfirm.repository;

import com.example.transportfirm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    // JOIN FETCH to avoid N+1 on employee.role access
    @Query("SELECT u FROM User u JOIN FETCH u.employee WHERE u.email = :email")
    Optional<User> findByEmailWithEmployee(@Param("email") String email);

    // Atomic tokenVersion increment for logout revocation
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.email = :email")
    void incrementTokenVersion(@Param("email") String email);
}
