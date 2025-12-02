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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacja ContactService.
 * - SRP: tylko logika biznesowa kontaktów
 * - DIP: konsumenci używają interfejsu ContactService
 */
@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    public ContactServiceImpl(ContactRepository contactRepository,
                              UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Contact> getAllContacts() {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return contactRepository.findAll();
        } else {
            return contactRepository.findByOwnerUsername(username);
        }
    }

    @Override
    public Contact getContact(Long id) {
        Contact contact = contactRepository.findById(id).orElse(null);
        if (contact == null) return null;

        if (isOwnerOrAdmin(contact)) {
            return contact;
        }
        throw new RuntimeException("Forbidden");
    }

    @Override
    public Contact addContact(Contact contact) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("Admin cannot create contacts");
        }

        contact.setOwnerUsername(auth.getName());
        return contactRepository.save(contact);
    }

    @Override
    public Contact updateContact(Long id, Contact updated) {
        return contactRepository.findById(id)
                .map(existing -> {
                    if (!isOwnerOrAdmin(existing)) throw new RuntimeException("Forbidden");
                    existing.setFirstName(updated.getFirstName());
                    existing.setLastName(updated.getLastName());
                    existing.setEmail(updated.getEmail());
                    existing.setPhone(updated.getPhone());
                    return contactRepository.save(existing);
                })
                .orElse(null);
    }

    @Override
    public boolean deleteContact(Long id) {
        return contactRepository.findById(id)
                .map(contact -> {
                    if (!isOwnerOrAdmin(contact)) throw new RuntimeException("Forbidden");
                    contactRepository.delete(contact);
                    return true;
                }).orElse(false);
    }

    @Override
    public String exportToJson() throws Exception {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        List<Contact> list = "ROLE_ADMIN".equals(user.getRole())
                ? contactRepository.findAll()
                : contactRepository.findByOwnerUsername(username);

        List<Contact> cleaned = list.stream()
                .map(c -> Contact.builder()
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .build())
                .collect(Collectors.toList());

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cleaned);
    }

    @Override
    public String exportToXml() throws Exception {
        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        List<Contact> list = "ROLE_ADMIN".equals(user.getRole())
                ? contactRepository.findAll()
                : contactRepository.findByOwnerUsername(username);

        List<Contact> cleaned = list.stream()
                .map(c -> Contact.builder()
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phone(c.getPhone())
                        .build())
                .collect(Collectors.toList());

        ContactsExportDTO dto = new ContactsExportDTO(cleaned);
        return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
    }

    @Override
    public void importFromJson(String json) throws Exception {
        List<Contact> contacts = objectMapper.readValue(json, new TypeReference<List<Contact>>() {});
        replaceContacts(contacts);
    }

    @Override
    public void importFromXml(String xml) throws Exception {
        replaceContactsFromXml(xml);
    }

    @Override
    @Transactional
    public void replaceContacts(List<Contact> contacts) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) throw new AccessDeniedException("Admin cannot import contacts");

        String username = getCurrentUsername();

        if (contacts == null) contacts = List.of();

        contacts.forEach(c -> {
            c.setOwnerUsername(username);
            c.setId(null);
        });

        contactRepository.deleteByOwnerUsername(username);
        contactRepository.saveAll(contacts);
    }

    @Override
    @Transactional
    public void replaceContactsFromXml(String xml) throws Exception {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) throw new AccessDeniedException("Admin cannot import contacts");

        String username = getCurrentUsername();

        List<Contact> contacts;
        try {
            ContactsExportDTO dto = xmlMapper.readValue(xml, ContactsExportDTO.class);
            contacts = dto.getContacts();
        } catch (Exception ex) {
            contacts = xmlMapper.readValue(xml, new TypeReference<List<Contact>>() {});
        }

        if (contacts == null) contacts = List.of();

        contacts.forEach(c -> {
            c.setOwnerUsername(username);
            c.setId(null);
        });

        contactRepository.deleteByOwnerUsername(username);
        contactRepository.saveAll(contacts);
    }

    // helpers
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
}