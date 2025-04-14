package com.brainchain.career_counselling.Career_Counselling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByRole(User.Role role);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndEmailVerified(String email, boolean emailVerified);
}
