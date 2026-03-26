package com.crm.personal.crm.contact;

import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final CustomerService customerService;

    public ContactService(ContactRepository contactRepository, CustomerService customerService) {
        this.contactRepository = contactRepository;
        this.customerService = customerService;
    }

    public List<ContactResponse> getContacts(Long customerId) {
        List<Contact> contacts = customerId == null
                ? contactRepository.findAll()
                : contactRepository.findByCustomerId(customerId);

        return contacts.stream()
                .map(ContactResponse::from)
                .collect(Collectors.toList());
    }

    public ContactResponse getContact(Long id) {
        return ContactResponse.from(findContact(id));
    }

    @Transactional
    public ContactResponse createContact(ContactRequest request) {
        Customer customer = customerService.findCustomer(request.getCustomerId());

        Contact contact = new Contact();
        applyRequest(contact, request, customer);
        LocalDateTime now = LocalDateTime.now();
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        return ContactResponse.from(contactRepository.save(contact));
    }

    @Transactional
    public ContactResponse updateContact(Long id, ContactRequest request) {
        Contact contact = findContact(id);
        Customer customer = customerService.findCustomer(request.getCustomerId());

        applyRequest(contact, request, customer);
        contact.setUpdatedAt(LocalDateTime.now());

        return ContactResponse.from(contactRepository.save(contact));
    }

    @Transactional
    public void deleteContact(Long id) {
        contactRepository.delete(findContact(id));
    }

    private Contact findContact(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));
    }

    private void applyRequest(Contact contact, ContactRequest request, Customer customer) {
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setJobTitle(request.getJobTitle());
        contact.setNotes(request.getNotes());
        contact.setCustomer(customer);
    }
}
