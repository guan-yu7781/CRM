package com.crm.personal.crm.maintenance;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.project.ProjectRecord;
import com.crm.personal.crm.project.ProjectService;
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
    private final ProjectService projectService;

    public AnnualMaintenanceService(AnnualMaintenanceMapper annualMaintenanceMapper,
                                    CustomerService customerService,
                                    ProjectService projectService) {
        this.annualMaintenanceMapper = annualMaintenanceMapper;
        this.customerService = customerService;
        this.projectService = projectService;
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
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());
        ProjectRecord project = resolveProject(request.getProjectId(), customer.getId());
        validateDates(request);
        validateUniqueYear(request, null);

        AnnualMaintenanceRecord record = new AnnualMaintenanceRecord();
        applyRequest(record, request, customer, project);
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        annualMaintenanceMapper.insert(record);
        return AnnualMaintenanceResponse.from(findRecord(record.getId()));
    }

    @Transactional
    public List<AnnualMaintenanceResponse> createRecords(List<AnnualMaintenanceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one annual maintenance record is required");
        }

        validateBatchYears(requests);

        return requests.stream()
                .map(this::createRecord)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRecord(Long id) {
        findRecord(id);
        annualMaintenanceMapper.deleteById(id);
    }

    @Transactional
    public AnnualMaintenanceResponse updateRecord(Long id, AnnualMaintenanceRequest request) {
        AnnualMaintenanceRecord record = findRecord(id);
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());
        ProjectRecord project = resolveProject(request.getProjectId(), customer.getId());
        validateDates(request);
        validateUniqueYearForUpdate(record, request, customer.getId());

        applyRequest(record, request, customer, project);
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

    private void validateBatchYears(List<AnnualMaintenanceRequest> requests) {
        for (int i = 0; i < requests.size(); i++) {
            AnnualMaintenanceRequest current = requests.get(i);
            for (int j = i + 1; j < requests.size(); j++) {
                AnnualMaintenanceRequest next = requests.get(j);
                if (current.getProjectId().equals(next.getProjectId())
                        && current.getMaintenanceYear().equals(next.getMaintenanceYear())) {
                    throw new IllegalArgumentException(
                            "Duplicate maintenance year " + current.getMaintenanceYear() + " for the selected project"
                    );
                }
            }
        }
    }

    private void validateUniqueYear(AnnualMaintenanceRequest request, Long currentRecordId) {
        AnnualMaintenanceRecord existing = annualMaintenanceMapper.findByCustomerIdAndProjectIdAndMaintenanceYear(
                request.getCustomerId(),
                request.getProjectId(),
                request.getMaintenanceYear()
        );
        if (existing != null && (currentRecordId == null || !existing.getId().equals(currentRecordId))) {
            throw new IllegalArgumentException(
                    "Maintenance year " + request.getMaintenanceYear() + " already exists for the selected project"
            );
        }
    }

    private void validateUniqueYearForUpdate(AnnualMaintenanceRecord record,
                                             AnnualMaintenanceRequest request,
                                             Long customerId) {
        boolean sameProject = record.getProjectId() != null && record.getProjectId().equals(request.getProjectId());
        boolean sameYear = record.getMaintenanceYear() != null && record.getMaintenanceYear().equals(request.getMaintenanceYear());
        if (sameProject && sameYear) {
            return;
        }

        int conflicts = annualMaintenanceMapper.countConflictsExcludingId(
                customerId,
                request.getProjectId(),
                request.getMaintenanceYear(),
                record.getId()
        );
        if (conflicts > 0) {
            throw new IllegalArgumentException(
                    "Maintenance year " + request.getMaintenanceYear() + " already exists for the selected project"
            );
        }
    }

    private ProjectRecord resolveProject(Long projectId, Long customerId) {
        ProjectRecord project = projectService.findProjectRecord(projectId);
        if (!project.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Selected project does not belong to the provided customer");
        }
        return project;
    }

    private void applyRequest(AnnualMaintenanceRecord record,
                              AnnualMaintenanceRequest request,
                              CustomerRecord customer,
                              ProjectRecord project) {
        record.setProjectId(project.getId());
        record.setProjectName(project.getProjectName());
        record.setMarket(project.getMarket());
        record.setMaintenanceYear(request.getMaintenanceYear());
        record.setAmount(request.getAmount());
        record.setCurrency(request.getCurrency() == null ? "USD" : request.getCurrency());
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        record.setPaymentStatus(request.getPaymentStatus());
        record.setRenewStatus(request.getRenewStatus());
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
    }
}
