package com.example.contacts.service;

import com.example.contacts.model.Contact;

import java.util.List;

/**
 * Interfejs ContactService
 * - I: segreguje API operacji na kontaktach
 * - D: kontrolery zależą od tej abstrakcji
 */
public interface ContactService {
    List<Contact> getAllContacts();
    Contact getContact(Long id);
    Contact addContact(Contact contact);
    Contact updateContact(Long id, Contact contact);
    boolean deleteContact(Long id);

    String exportToJson() throws Exception;
    String exportToXml() throws Exception;
    void importFromJson(String json) throws Exception;
    void importFromXml(String xml) throws Exception;

    void replaceContacts(List<Contact> contacts);
    void replaceContactsFromXml(String xml) throws Exception;
}