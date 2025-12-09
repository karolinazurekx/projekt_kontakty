package com.example.contacts.service;

import com.example.contacts.TestSecurityUtils;
import com.example.contacts.dto.ContactsExportDTO;
import com.example.contacts.model.AppUser;
import com.example.contacts.model.Contact;
import com.example.contacts.repository.ContactRepository;
import com.example.contacts.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    @Mock
    ContactRepository contactRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    Validator validator;

    @InjectMocks
    ContactService contactService;

    AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        TestSecurityUtils.clear();
    }

    private AppUser user(String username, String role) {
        return AppUser.builder().username(username).password("x").role(role).build();
    }

    // 1. getAllContacts — user gets own
    @Test
    void getAllContacts_returnsOnlyUserContacts() {
        TestSecurityUtils.setAuthentication("alice", "ROLE_USER");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user("alice", "ROLE_USER")));
        when(contactRepository.findByOwnerUsername("alice")).thenReturn(List.of(
                Contact.builder().id(1L).ownerUsername("alice").firstName("A").lastName("B").email("a@b").phone("123456789").build()
        ));

        var res = contactService.getAllContacts();
        assertThat(res).hasSize(1);
        verify(contactRepository).findByOwnerUsername("alice");
    }

    // 2. getAllContacts — admin gets all
    @Test
    void getAllContacts_adminReturnsAll() {
        TestSecurityUtils.setAuthentication("admin", "ROLE_ADMIN");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user("admin", "ROLE_ADMIN")));
        when(contactRepository.findAll()).thenReturn(List.of(new Contact()));

        var res = contactService.getAllContacts();
        assertThat(res).isNotEmpty();
        verify(contactRepository).findAll();
    }

    // 3. getContact — owner allowed
    @Test
    void getContact_ownerAllowed() {
        TestSecurityUtils.setAuthentication("bob", "ROLE_USER");
        Contact c = Contact.builder().id(2L).ownerUsername("bob").firstName("F").lastName("L").email("e@e").phone("123456789").build();
        when(contactRepository.findById(2L)).thenReturn(Optional.of(c));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user("bob","ROLE_USER")));

        Contact found = contactService.getContact(2L);
        assertThat(found).isEqualTo(c);
    }

    // 4. getContact — forbidden for non-owner
    @Test
    void getContact_nonOwnerThrows() {
        TestSecurityUtils.setAuthentication("eve", "ROLE_USER");
        Contact c = Contact.builder().id(3L).ownerUsername("alice").build();
        when(contactRepository.findById(3L)).thenReturn(Optional.of(c));
        when(userRepository.findByUsername("eve")).thenReturn(Optional.of(user("eve","ROLE_USER")));

        assertThatThrownBy(() -> contactService.getContact(3L)).isInstanceOf(RuntimeException.class);
    }

    // 5. addContact — user can add
    @Test
    void addContact_userAddsContact() {
        TestSecurityUtils.setAuthentication("charlie", "ROLE_USER");
        Contact toAdd = Contact.builder().firstName("X").lastName("Y").email("x@y").phone("123456789").build();
        when(contactRepository.save(any())).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        Contact saved = contactService.addContact(toAdd);
        assertThat(saved.getOwnerUsername()).isEqualTo("charlie");
        assertThat(saved.getId()).isEqualTo(10L);
    }

    // 6. addContact — admin cannot create contacts
    @Test
    void addContact_adminThrows() {
        TestSecurityUtils.setAuthentication("admin", "ROLE_ADMIN");
        Contact c = Contact.builder().firstName("A").lastName("B").email("a@b").phone("123456789").build();
        assertThatThrownBy(() -> contactService.addContact(c)).isInstanceOf(RuntimeException.class);
    }

    // 7. updateContact — success
    @Test
    void updateContact_success() {
        TestSecurityUtils.setAuthentication("dave", "ROLE_USER");
        Contact existing = Contact.builder().id(5L).ownerUsername("dave").firstName("Old").lastName("O").email("o@o").phone("123456789").build();
        Contact updated = Contact.builder().firstName("New").lastName("N").email("n@n").phone("987654321").build();

        when(contactRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(contactRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Contact res = contactService.updateContact(5L, updated);
        assertThat(res.getFirstName()).isEqualTo("New");
        assertThat(res.getPhone()).isEqualTo("987654321");
    }

    // 8. updateContact — not found returns null
    @Test
    void updateContact_notFound() {
        when(contactRepository.findById(99L)).thenReturn(Optional.empty());
        Contact any = new Contact();
        var res = contactService.updateContact(99L, any);
        assertThat(res).isNull();
    }

    // 9. deleteContact — success
    @Test
    void deleteContact_success() {
        TestSecurityUtils.setAuthentication("frank", "ROLE_USER");
        Contact c = Contact.builder().id(8L).ownerUsername("frank").build();
        when(contactRepository.findById(8L)).thenReturn(Optional.of(c));
        doNothing().when(contactRepository).delete(c);

        boolean res = contactService.deleteContact(8L);
        assertThat(res).isTrue();
        verify(contactRepository).delete(c);
    }

    // 10. replaceContacts — admin cannot import
    @Test
    void replaceContacts_adminCannotImport() {
        TestSecurityUtils.setAuthentication("admin", "ROLE_ADMIN");
        List<Contact> list = List.of(new Contact());
        assertThatThrownBy(() -> contactService.replaceContacts(list)).isInstanceOf(AccessDeniedException.class);
    }
}