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
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW')")
    public List<AnnualMaintenanceResponse> getRecords(@RequestParam(required = false) Long customerId) {
        return annualMaintenanceService.getRecords(customerId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW')")
    public AnnualMaintenanceResponse getRecord(@PathVariable Long id) {
        return annualMaintenanceService.getRecord(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAINTENANCE_CREATE')")
    public AnnualMaintenanceResponse createRecord(@Valid @RequestBody AnnualMaintenanceRequest request) {
        return annualMaintenanceService.createRecord(request);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MAINTENANCE_CREATE')")
    public List<AnnualMaintenanceResponse> createRecords(@Valid @RequestBody List<@Valid AnnualMaintenanceRequest> requests) {
        return annualMaintenanceService.createRecords(requests);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MAINTENANCE_EDIT','MAINTENANCE_UPDATE_PAYMENT','MAINTENANCE_UPDATE_RENEW')")
    public AnnualMaintenanceResponse updateRecord(@PathVariable Long id, @Valid @RequestBody AnnualMaintenanceRequest request) {
        return annualMaintenanceService.updateRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('MAINTENANCE_DELETE')")
    public void deleteRecord(@PathVariable Long id) {
        annualMaintenanceService.deleteRecord(id);
    }
}
