package com.crm.personal.crm.maintenance;

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
@RequestMapping("/api/annual-maintenance")
public class AnnualMaintenanceController {

    private final AnnualMaintenanceService annualMaintenanceService;

    public AnnualMaintenanceController(AnnualMaintenanceService annualMaintenanceService) {
        this.annualMaintenanceService = annualMaintenanceService;
    }

    @GetMapping
    public List<AnnualMaintenanceResponse> getRecords(@RequestParam(required = false) Long customerId) {
        return annualMaintenanceService.getRecords(customerId);
    }

    @GetMapping("/{id}")
    public AnnualMaintenanceResponse getRecord(@PathVariable Long id) {
        return annualMaintenanceService.getRecord(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnualMaintenanceResponse createRecord(@Valid @RequestBody AnnualMaintenanceRequest request) {
        return annualMaintenanceService.createRecord(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<AnnualMaintenanceResponse> createRecords(@Valid @RequestBody List<@Valid AnnualMaintenanceRequest> requests) {
        return annualMaintenanceService.createRecords(requests);
    }

    @PutMapping("/{id}")
    public AnnualMaintenanceResponse updateRecord(@PathVariable Long id, @Valid @RequestBody AnnualMaintenanceRequest request) {
        return annualMaintenanceService.updateRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRecord(@PathVariable Long id) {
        annualMaintenanceService.deleteRecord(id);
    }
}
