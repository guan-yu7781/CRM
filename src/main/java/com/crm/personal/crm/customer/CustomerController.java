package com.crm.personal.crm.customer;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getCustomers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "500") int size) {
        return customerService.getAllCustomers(page, size);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerService.getCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
}
