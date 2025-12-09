package com.example.contacts.repository;

import com.example.contacts.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium dla kontaków
 * - S: dostęp do DB
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOwnerUsername(String ownerUsername);

    long deleteByOwnerUsername(String ownerUsername);
}