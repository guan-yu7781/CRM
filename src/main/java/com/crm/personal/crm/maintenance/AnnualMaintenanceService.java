package com.crm.personal.crm.maintenance;

import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnualMaintenanceService {

    private final AnnualMaintenanceMapper annualMaintenanceMapper;
    private final CustomerService customerService;

    public AnnualMaintenanceService(AnnualMaintenanceMapper annualMaintenanceMapper,
                                    CustomerService customerService) {
        this.annualMaintenanceMapper = annualMaintenanceMapper;
        this.customerService = customerService;
    }

    public List<AnnualMaintenanceResponse> getRecords(Long customerId) {
        List<AnnualMaintenanceRecord> records;
        if (customerId != null) {
            customerService.findCustomer(customerId);
            records = annualMaintenanceMapper.findByCustomerId(customerId);
        } else {
            throw new IllegalArgumentException("Customer id is required");
        }

        return records.stream()
                .map(AnnualMaintenanceResponse::from)
                .collect(Collectors.toList());
    }

    public AnnualMaintenanceResponse getRecord(Long id) {
        return AnnualMaintenanceResponse.from(findRecord(id));
    }

    @Transactional
    public AnnualMaintenanceResponse createRecord(AnnualMaintenanceRequest request) {
        Customer customer = customerService.findCustomer(request.getCustomerId());
        validateDates(request);

        AnnualMaintenanceRecord record = new AnnualMaintenanceRecord();
        applyRequest(record, request, customer);
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        annualMaintenanceMapper.insert(record);
        return AnnualMaintenanceResponse.from(findRecord(record.getId()));
    }

    @Transactional
    public AnnualMaintenanceResponse updateRecord(Long id, AnnualMaintenanceRequest request) {
        AnnualMaintenanceRecord record = findRecord(id);
        Customer customer = customerService.findCustomer(request.getCustomerId());
        validateDates(request);

        applyRequest(record, request, customer);
        record.setUpdatedAt(LocalDateTime.now());

        annualMaintenanceMapper.update(record);
        return AnnualMaintenanceResponse.from(findRecord(id));
    }

    private AnnualMaintenanceRecord findRecord(Long id) {
        AnnualMaintenanceRecord record = annualMaintenanceMapper.findById(id);
        if (record == null) {
            throw new ResourceNotFoundException("Annual maintenance record not found with id " + id);
        }
        return record;
    }

    private void validateDates(AnnualMaintenanceRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be earlier than start date");
        }
    }

    private void applyRequest(AnnualMaintenanceRecord record, AnnualMaintenanceRequest request, Customer customer) {
        record.setProjectName(request.getProjectName());
        record.setMarket(request.getMarket());
        record.setMaintenanceYear(request.getMaintenanceYear());
        record.setAmount(request.getAmount());
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setPaymentStatus(request.getPaymentStatus());
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
    }
}
