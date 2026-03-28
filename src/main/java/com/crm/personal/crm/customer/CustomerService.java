package com.crm.personal.crm.customer;

import com.crm.personal.crm.audit.AuditAction;
import com.crm.personal.crm.audit.AuditService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final CifNumberGenerator cifNumberGenerator;
    private final AuditService auditService;

    public CustomerService(CustomerMapper customerMapper, CustomerRepository customerRepository,
                           CifNumberGenerator cifNumberGenerator, AuditService auditService) {
        this.customerMapper = customerMapper;
        this.customerRepository = customerRepository;
        this.cifNumberGenerator = cifNumberGenerator;
        this.auditService = auditService;
    }

    public List<CustomerResponse> getAllCustomers(int page, int size) {
        return customerMapper.findPaged(size, page * size)
                .stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomer(Long id) {
        return CustomerResponse.from(findCustomerRecord(id));
    }

    public String nextCifNumber(CustomerType type) {
        return cifNumberGenerator.next(type);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Auto-generate CIF if not provided
        if (request.getCifNumber() == null || request.getCifNumber().isBlank()) {
            CustomerType type = request.getCustomerType() == null ? CustomerType.COMMERCIAL_BANK : request.getCustomerType();
            request.setCifNumber(cifNumberGenerator.next(type));
        }

        CustomerRecord existingByCif = customerMapper.findByCifNumberIgnoreCase(request.getCifNumber());
        if (existingByCif != null) {
            throw new IllegalArgumentException("A customer with this CIF number already exists");
        }

        CustomerRecord customer = new CustomerRecord();
        applyRequest(customer, request);

        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        customerMapper.insert(customer);
        CustomerResponse result = CustomerResponse.from(findCustomerRecord(customer.getId()));
        auditService.log("CUSTOMER", result.getId(), result.getName(), AuditAction.CREATE,
                "Created customer '" + result.getName() + "' (" + result.getCustomerType() + ")");
        return result;
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        CustomerRecord customer = findCustomerRecord(id);

        CustomerRecord existingByCif = customerMapper.findByCifNumberIgnoreCase(request.getCifNumber());
        if (existingByCif != null && !existingByCif.getId().equals(id)) {
            throw new IllegalArgumentException("A customer with this CIF number already exists");
        }

        applyRequest(customer, request);
        customer.setUpdatedAt(LocalDateTime.now());

        customerMapper.update(customer);
        CustomerResponse result = CustomerResponse.from(findCustomerRecord(id));
        auditService.log("CUSTOMER", id, result.getName(), AuditAction.UPDATE,
                "Updated customer '" + result.getName() + "'");
        return result;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CustomerRecord customer = findCustomerRecord(id);
        String name = customer.getName();
        customerMapper.deleteById(id);
        auditService.log("CUSTOMER", id, name, AuditAction.DELETE,
                "Deleted customer '" + name + "'");
    }

    public CustomerRecord findCustomerRecord(Long id) {
        CustomerRecord customer = customerMapper.findById(id);
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found with id " + id);
        }
        return customer;
    }

    public Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    public CustomerRecord findFirstCustomerRecord() {
        return customerMapper.findFirst();
    }

    private void applyRequest(CustomerRecord customer, CustomerRequest request) {
        customer.setName(request.getName());
        customer.setCustomerType(request.getCustomerType() == null ? CustomerType.COMMERCIAL_BANK : request.getCustomerType());
        customer.setCifNumber(request.getCifNumber());
        customer.setStatus(request.getStatus() == null ? CustomerStatus.LEAD : request.getStatus());
        customer.setRiskLevel(request.getRiskLevel() == null ? RiskLevel.LOW : request.getRiskLevel());
        customer.setNotes(request.getNotes());
    }
}
