package com.crm.personal.crm.contact;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTACT_VIEW')")
    public List<ContactResponse> getContacts(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return contactService.getContacts(customerId, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACT_VIEW')")
    public ContactResponse getContact(@PathVariable Long id) {
        return contactService.getContact(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTACT_CREATE')")
    public ContactResponse createContact(@Valid @RequestBody ContactRequest request) {
        return contactService.createContact(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACT_EDIT')")
    public ContactResponse updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        return contactService.updateContact(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CONTACT_DELETE')")
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
    }
}
