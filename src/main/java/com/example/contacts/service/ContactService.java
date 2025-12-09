package com.example.contacts.service;

import com.example.contacts.dto.ContactsExportDTO;
import com.example.contacts.model.AppUser;
import com.example.contacts.model.Contact;
import com.example.contacts.repository.ContactRepository;
import com.example.contacts.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    private final Validator validator;

    public ContactService(ContactRepository contactRepository,
                          UserRepository userRepository,
                          Validator validator) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    // =========================================
    // GET ALL CONTACTS — ADMIN ALL / USER OWN
    // =========================================
    public List<Contact> getAllContacts() {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        if (user.getRole().equals("ROLE_ADMIN")) {
            return contactRepository.findAll();
        } else {
            return contactRepository.findByOwnerUsername(username);
        }
    }

    // =========================================
    // GET ONE — only own unless admin
    // =========================================
    public Contact getContact(Long id) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if (contact == null) return null;

        if (isOwnerOrAdmin(contact)) {
            return contact;
        }
        throw new RuntimeException("Forbidden");
    }

    // =========================================
    // ADD CONTACT — assign ownerr
    // =========================================
    public Contact addContact(Contact contact) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Admin cannot create contacts");
        }

        contact.setOwnerUsername(auth.getName());

        // validate entity (controller also validates, but be defensive)
        validateContact(contact);

        return contactRepository.save(contact);
    }

    // =========================================
    // UPDATE — only own unless admin
    // =========================================
    public Contact updateContact(Long id, Contact updated) {
        return contactRepository.findById(id)
                .map(existing -> {
                    if (!isOwnerOrAdmin(existing))
                        throw new RuntimeException("Forbidden");

                    existing.setFirstName(updated.getFirstName());
                    existing.setLastName(updated.getLastName());
                    existing.setEmail(updated.getEmail());
                    existing.setPhone(updated.getPhone());

                    // validate before save
                    validateContact(existing);

                    return contactRepository.save(existing);
                })
                .orElse(null);
    }

    // =========================================
    // DELETE — only own unless admin
    // =========================================
    public boolean deleteContact(Long id) {
        return contactRepository.findById(id)
                .map(contact -> {
                    if (!isOwnerOrAdmin(contact))
                        throw new RuntimeException("Forbidden");

                    contactRepository.delete(contact);
                    return true;
                })
                .orElse(false);
    }

    // =========================================
    // EXPORT JSON
    // - removes ownerUsername and id from exported objects so they can be imported by another user
    // =========================================
    public String exportToJson() throws Exception {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        List<Contact> list = user.getRole().equals("ROLE_ADMIN")
                ? contactRepository.findAll()
                : contactRepository.findByOwnerUsername(username);

        // create cleaned copies without id and ownerUsername
        List<Contact> cleaned = list.stream()
                .map(c -> Contact.builder()
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .build()
                )
                .collect(Collectors.toList());

        return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(cleaned);
    }

    // =========================================
    // IMPORT JSON — owner = logged user
    // Deleguje do replaceContacts aby import był atomowy i bez konfliktów id
    // =========================================
    public void importFromJson(String json) throws Exception {
        List<Contact> contacts = objectMapper.readValue(
                json,
                new TypeReference<List<Contact>>() {}
        );
        replaceContacts(contacts);
    }

    // =========================================
    // EXPORT XML
    // - removes ownerUsername and id from exported XML so it can be imported by another user
    // =========================================
    public String exportToXml() throws Exception {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        List<Contact> list = user.getRole().equals("ROLE_ADMIN")
                ? contactRepository.findAll()
                : contactRepository.findByOwnerUsername(username);

        // create cleaned copies without id and ownerUsername
        List<Contact> cleaned = list.stream()
                .map(c -> Contact.builder()
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .build()
                )
                .collect(Collectors.toList());

        ContactsExportDTO dto = new ContactsExportDTO(cleaned);

        return xmlMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(dto);
    }

    // =========================================
    // IMPORT XML
    // Deleguje do replaceContactsFromXml aby import był atomowy i bez konfliktów id
    // =========================================
    public void importFromXml(String xml) throws Exception {
        replaceContactsFromXml(xml);
    }

    // =========================================
    // NOWA METODA: atomowo zastąp kontakty zalogowanego użytkownika listą contacts
    // - oznaczona @Transactional
    // - usuwa istniejące kontakty w DB jednym zapytaniem (deleteByOwnerUsername)
    // - ustawia ownerUsername i id=null dla importowanych rekordów żeby JPA wykonało INSERT
    // =========================================
    @Transactional
    public void replaceContacts(List<Contact> contacts) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("Admin cannot import contacts");
        }

        String username = getCurrentUsername();

        if (contacts == null) contacts = List.of();

        // przygotuj importowane kontakty: ustaw ownera i usuń id
        contacts.forEach(c -> {
            c.setOwnerUsername(username);
            c.setId(null);
        });

        // waliduj każdą pozycję
        contacts.forEach(this::validateContact);

        // usuń wszystkie kontakty użytkownika jednym zapytaniem (musisz mieć deleteByOwnerUsername w repo)
        contactRepository.deleteByOwnerUsername(username);

        // zapisz nowe kontakty (INSERT)
        contactRepository.saveAll(contacts);
    }

    // =========================================
    // NOWA METODA: import z XML — obsługuje XML będący listą lub wrapperem ContactsExportDTO
    // - oznaczona @Transactional
    // =========================================
    @Transactional
    public void replaceContactsFromXml(String xml) throws Exception {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("Admin cannot import contacts");
        }

        String username = getCurrentUsername();

        List<Contact> contacts;

        // najpierw spróbuj z wrapperem ContactsExportDTO
        try {
            ContactsExportDTO dto = xmlMapper.readValue(xml, ContactsExportDTO.class);
            contacts = dto.getContacts();
        } catch (Exception ex) {
            // fallback: spróbuj bez wrappera jako lista
            contacts = xmlMapper.readValue(xml, new TypeReference<List<Contact>>() {});
        }

        if (contacts == null) contacts = List.of();

        contacts.forEach(c -> {
            c.setOwnerUsername(username);
            c.setId(null);
        });

        // waliduj każdą pozycję
        contacts.forEach(this::validateContact);

        contactRepository.deleteByOwnerUsername(username);
        contactRepository.saveAll(contacts);
    }

    // =========================================
    // HELPERS
    // =========================================

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isOwnerOrAdmin(Contact c) {
        String username = getCurrentUsername();
        if (c.getOwnerUsername().equals(username)) return true;

        return userRepository.findByUsername(username)
                .map(u -> u.getRole().equals("ROLE_ADMIN"))
                .orElse(false);
    }

    private void validateContact(Contact c) {
        Set<ConstraintViolation<Contact>> violations = validator.validate(c);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}