package com.example.contacts.service;

import com.example.contacts.model.Contact;

import java.util.List;

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

    // lower-level helpers used by controller/tests
    void replaceContacts(java.util.List<Contact> contacts);
    void replaceContactsFromXml(String xml) throws Exception;
}