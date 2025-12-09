package com.example.contacts.service;

import com.example.contacts.dto.ContactsExportDTO;
import com.example.contacts.model.AppUser;
import com.example.contacts.model.Contact;
import com.example.contacts.repository.ContactRepository;
import com.example.contacts.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**

 * - S: logika biznesowa dotyczaca kontaktów
 * - D: zależy od abstrakcji repozytoriów (ContactRepository, UserRepository)
 * - O: można rozszerzyć zachowanie przez dekoratory / proxy
 */
@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    private final Validator validator;

    public ContactServiceImpl(ContactRepository contactRepository,
                              UserRepository userRepository,
                              Validator validator) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.validator = validator;
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
        if (isOwnerOrAdmin(contact)) return contact;
        throw new RuntimeException("Forbidden");
    }

    @Override
    public Contact addContact(Contact contact) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("Admin cannot create contacts");
        }
        contact.setOwnerUsername(auth.getName());
        validateContact(contact);
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

                    validateContact(existing);
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
    public void importFromJson(String json) throws Exception {
        List<Contact> contacts = objectMapper.readValue(json, new TypeReference<List<Contact>>() {});
        replaceContacts(contacts);
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
    public void importFromXml(String xml) throws Exception {
        replaceContactsFromXml(xml);
    }

    @Override
    @Transactional
    public void replaceContacts(List<Contact> contacts) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) {
            throw new AccessDeniedException("Admin cannot import contacts");
        }
        String username = getCurrentUsername();

        if (contacts == null) contacts = List.of();

        contacts.forEach(c -> {
            c.setOwnerUsername(username);
            c.setId(null);
        });

        contacts.forEach(this::validateContact);

        contactRepository.deleteByOwnerUsername(username);
        contactRepository.saveAll(contacts);
    }

    @Override
    @Transactional
    public void replaceContactsFromXml(String xml) throws Exception {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_ADMIN".equals(role)) {
            throw new AccessDeniedException("Admin cannot import contacts");
        }
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

        contacts.forEach(this::validateContact);

        contactRepository.deleteByOwnerUsername(username);
        contactRepository.saveAll(contacts);
    }

    // HELPERS
    private String getCurrentUsername() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isOwnerOrAdmin(Contact c) {
        String username = getCurrentUsername();
        if (c.getOwnerUsername().equals(username)) return true;

        return userRepository.findByUsername(username)
                .map(u -> "ROLE_ADMIN".equals(u.getRole()))
                .orElse(false);
    }

    private void validateContact(Contact c) {
        Set<ConstraintViolation<Contact>> violations = validator.validate(c);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}