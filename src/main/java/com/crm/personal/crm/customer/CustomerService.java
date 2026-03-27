package com.crm.personal.crm.customer;

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

    public CustomerService(CustomerMapper customerMapper, CustomerRepository customerRepository) {
        this.customerMapper = customerMapper;
        this.customerRepository = customerRepository;
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

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
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
        return CustomerResponse.from(findCustomerRecord(customer.getId()));
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
        return CustomerResponse.from(findCustomerRecord(id));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        findCustomerRecord(id);
        customerMapper.deleteById(id);
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
        customer.setSegment(request.getSegment() == null ? CustomerSegment.RETAIL : request.getSegment());
        customer.setStatus(request.getStatus() == null ? CustomerStatus.LEAD : request.getStatus());
        customer.setRiskLevel(request.getRiskLevel() == null ? RiskLevel.LOW : request.getRiskLevel());
        customer.setNotes(request.getNotes());
    }
}
