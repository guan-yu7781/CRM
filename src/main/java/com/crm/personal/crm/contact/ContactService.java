package com.crm.personal.crm.contact;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final ContactMapper contactMapper;
    private final CustomerService customerService;

    public ContactService(ContactMapper contactMapper, CustomerService customerService) {
        this.contactMapper = contactMapper;
        this.customerService = customerService;
    }

    public List<ContactResponse> getContacts(Long customerId) {
        List<ContactRecord> contacts = customerId == null
                ? contactMapper.findAll()
                : contactMapper.findByCustomerId(customerId);

        if (customerId != null) {
            customerService.findCustomerRecord(customerId);
        }

        return contacts.stream()
                .map(ContactResponse::from)
                .collect(Collectors.toList());
    }

    public ContactResponse getContact(Long id) {
        return ContactResponse.from(findContact(id));
    }

    @Transactional
    public ContactResponse createContact(ContactRequest request) {
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        ContactRecord contact = new ContactRecord();
        applyRequest(contact, request, customer);
        LocalDateTime now = LocalDateTime.now();
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        contactMapper.insert(contact);
        return ContactResponse.from(findContact(contact.getId()));
    }

    @Transactional
    public ContactResponse updateContact(Long id, ContactRequest request) {
        ContactRecord contact = findContact(id);
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        applyRequest(contact, request, customer);
        contact.setUpdatedAt(LocalDateTime.now());

        contactMapper.update(contact);
        return ContactResponse.from(findContact(id));
    }

    @Transactional
    public void deleteContact(Long id) {
        findContact(id);
        contactMapper.deleteById(id);
    }

    private ContactRecord findContact(Long id) {
        ContactRecord contact = contactMapper.findById(id);
        if (contact == null) {
            throw new ResourceNotFoundException("Contact not found with id " + id);
        }
        return contact;
    }

    private void applyRequest(ContactRecord contact, ContactRequest request, CustomerRecord customer) {
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setJobTitle(request.getJobTitle());
        contact.setNotes(request.getNotes());
        contact.setCustomerId(customer.getId());
        contact.setCustomerName(customer.getName());
    }
}
