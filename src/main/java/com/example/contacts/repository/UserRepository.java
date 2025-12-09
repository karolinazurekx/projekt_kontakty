package com.example.contacts.repository;

import com.example.contacts.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repozytorium użytkowników
 * - S: tylko dostęp do DB
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}