package com.example.contacts.controller;

import com.example.contacts.model.Contact;
import com.example.contacts.service.ContactService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactControllerTest {

    @Mock
    ContactService contactService;

    @InjectMocks
    ContactController contactController;

    AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // 1. getAll
    @Test
    void getAll_returnsList() {
        when(contactService.getAllContacts()).thenReturn(List.of(new Contact()));
        var res = contactController.getAll();
        assertThat(res).hasSize(1);
    }

    // 2. getOne found
    @Test
    void getOne_found() {
        Contact c = Contact.builder().id(1L).build();
        when(contactService.getContact(1L)).thenReturn(c);
        ResponseEntity<Contact> res = contactController.getOne(1L);
        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        assertThat(res.getBody()).isEqualTo(c);
    }

    // 3. getOne not found
    @Test
    void getOne_notFound() {
        when(contactService.getContact(2L)).thenReturn(null);
        ResponseEntity<Contact> res = contactController.getOne(2L);
        assertThat(res.getStatusCodeValue()).isEqualTo(404);
    }

    // 4. add contact
    @Test
    void add_callsServiceAndReturns() {
        Contact in = Contact.builder().firstName("A").lastName("B").email("a@b").phone("123456789").build();
        Contact saved = Contact.builder().id(5L).firstName("A").build();
        when(contactService.addContact(in)).thenReturn(saved);
        ResponseEntity<Contact> res = contactController.add(in);
        assertThat(res.getBody().getId()).isEqualTo(5L);
    }

    // 5. exportJson returns string
    @Test
    void exportJson_returnsString() throws Exception {
        when(contactService.exportToJson()).thenReturn("[]");
        String json = contactController.exportJson();
        assertThat(json).isEqualTo("[]");
    }

    // 6. importJson delegates and returns ok
    @Test
    void importJson_delegates() throws Exception {
        List<Contact> list = List.of(Contact.builder().firstName("A").lastName("B").email("a@b").phone("123456789").build());
        doNothing().when(contactService).replaceContacts(list);
        ResponseEntity<?> res = contactController.importJson(list);
        assertThat(res.getStatusCodeValue()).isEqualTo(200);
        verify(contactService).replaceContacts(list);
    }
}