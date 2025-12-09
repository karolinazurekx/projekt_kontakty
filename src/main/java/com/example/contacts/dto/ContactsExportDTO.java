package com.example.contacts.dto;

import com.example.contacts.model.Contact;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "contacts")
public class ContactsExportDTO {

    // każdy kontakt będzie reprezentowany jako <contact>...</contact> wewnątrz <contacts>
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "contact")
    private List<Contact> contacts;
}