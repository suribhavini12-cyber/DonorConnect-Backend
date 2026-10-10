package com.donorconnect.repository;

import com.donorconnect.model.Role;
import com.donorconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByActiveTrue();
    long countByRole(Role role);
    long countByRoleAndActiveTrue(Role role);
    boolean existsByRole(Role role);
}
