package com.example.contacts.controller;

import com.example.contacts.model.Contact;
import com.example.contacts.service.ContactService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@Validated
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }
    // GET: wszystkie kontakty zalogowanego użytkownika (admin: wszystkie)
    @GetMapping
    public List<Contact> getAll() {
        return contactService.getAllContacts();
    }

    // GET: pojedynczy kontakt (user: tylko swój, admin: każdy)
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getOne(@PathVariable Long id) {
        Contact contact = contactService.getContact(id);
        if (contact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contact);
    }

    // POST: dodaj kontakt – zawsze przypisany do zalogowanego
    @PostMapping
    public ResponseEntity<Contact> add(@Valid @RequestBody Contact contact) {
        Contact saved = contactService.addContact(contact);
        return ResponseEntity.ok(saved);
    }

    // PUT: aktualizuj kontakt – user tylko swoje, admin dowolny
    @PutMapping("/{id}")
    public ResponseEntity<Contact> update(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        Contact updated = contactService.updateContact(id, contact);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // DELETE: user usuwa swoje, admin każdy
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = contactService.deleteContact(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            // 404 – albo nie istnieje, albo nie jest Twój (dla usera)
            return ResponseEntity.notFound().build();
        }
    }

    // EXPORT / IMPORT

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