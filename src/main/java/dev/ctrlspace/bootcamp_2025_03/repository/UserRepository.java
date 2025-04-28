package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE email = ?1")
    Optional<User> findByEmail(String email);




}
