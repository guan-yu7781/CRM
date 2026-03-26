package com.crm.personal.crm.maintenance;

import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AnnualMaintenanceSeedRunner implements ApplicationRunner {

    private final AnnualMaintenanceMapper annualMaintenanceMapper;
    private final CustomerRepository customerRepository;

    public AnnualMaintenanceSeedRunner(AnnualMaintenanceMapper annualMaintenanceMapper,
                                       CustomerRepository customerRepository) {
        this.annualMaintenanceMapper = annualMaintenanceMapper;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Optional<Customer> customerOptional = customerRepository.findAll().stream().findFirst();
        if (customerOptional.isEmpty()) {
            return;
        }

        Customer customer = customerOptional.get();
        seedRecord(customer, 1, LocalDate.of(2024, 1, 15), LocalDate.of(2025, 1, 14), PaymentStatus.PAID, new BigDecimal("18500.00"));
        seedRecord(customer, 2, LocalDate.of(2025, 2, 1), LocalDate.of(2026, 1, 31), PaymentStatus.NOT_PAID, new BigDecimal("21000.00"));
        seedRecord(customer, 3, LocalDate.of(2026, 3, 1), LocalDate.of(2027, 2, 28), PaymentStatus.PAID, new BigDecimal("23000.00"));
    }

    private void seedRecord(Customer customer,
                            int year,
                            LocalDate startDate,
                            LocalDate endDate,
                            PaymentStatus paymentStatus,
                            BigDecimal amount) {
        if (annualMaintenanceMapper.countByCustomerIdAndProjectNameAndMaintenanceYear(
                customer.getId(), "NCBA Kenya MSL", year) > 0) {
            return;
        }

        AnnualMaintenanceRecord record = new AnnualMaintenanceRecord();
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
        record.setProjectName("NCBA Kenya MSL");
        record.setMarket("Kenya");
        record.setMaintenanceYear(year);
        record.setAmount(amount);
        record.setStartDate(startDate);
        record.setEndDate(endDate);
        record.setPaymentStatus(paymentStatus);
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        annualMaintenanceMapper.insert(record);
    }
}
