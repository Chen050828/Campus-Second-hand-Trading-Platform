package com.campusmarket.repository;

import com.campusmarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByStatus(User.UserStatus status);
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
    List<User> findByRole(User.UserRole role);
}
