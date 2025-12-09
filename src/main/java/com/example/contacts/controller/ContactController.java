package com.example.contacts.controller;

import com.example.contacts.model.Contact;
import com.example.contacts.service.ContactService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ContactController
 * - S: mapping HTTP -> wywołania serwisu
 * - D: używa abstrakcji ContactService (odwrocenie zaleznosci)
 * - I:  tylko potrzebne operacje
 */
@RestController
@RequestMapping("/api/contacts")
@Validated
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getOne(@PathVariable Long id) {
        Contact contact = contactService.getContact(id);
        if (contact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    public ResponseEntity<Contact> add(@Valid @RequestBody Contact contact) {
        Contact saved = contactService.addContact(contact);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> update(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        Contact updated = contactService.updateContact(id, contact);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = contactService.deleteContact(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String exportJson() throws Exception {
        return contactService.exportToJson();
    }

    @GetMapping(value = "/export/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String exportXml() throws Exception {
        return contactService.exportToXml();
    }

    @PostMapping("/import/json")
    public ResponseEntity<?> importJson(@Valid @RequestBody List<@Valid Contact> contacts) throws Exception {
        contactService.replaceContacts(contacts);
        return ResponseEntity.ok("Imported JSON");
    }

    @PostMapping(value = "/import/xml", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> importXml(@RequestBody String xml) throws Exception {
        contactService.replaceContactsFromXml(xml);
        return ResponseEntity.ok("Imported XML");
    }
}