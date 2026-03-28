package com.crm.personal.crm.project;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.deal.DealMapper;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final CustomerService customerService;
    private final DealMapper dealMapper;

    public ProjectService(ProjectMapper projectMapper, CustomerService customerService, DealMapper dealMapper) {
        this.projectMapper = projectMapper;
        this.customerService = customerService;
        this.dealMapper = dealMapper;
    }

    public List<ProjectResponse> getProjects(Long customerId, int page, int size) {
        List<ProjectRecord> projects = customerId == null
                ? projectMapper.findPaged(size, page * size)
                : projectMapper.findByCustomerId(customerId);

        if (customerId != null) {
            customerService.findCustomerRecord(customerId);
        }

        return projects.stream()
                .map(ProjectResponse::from)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(Long id) {
        return ProjectResponse.from(findProjectRecord(id));
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        ProjectRecord project = new ProjectRecord();
        applyRequest(project, request, customer);
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        projectMapper.insert(project);
        return ProjectResponse.from(findProjectRecord(project.getId()));
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        ProjectRecord project = findProjectRecord(id);
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        applyRequest(project, request, customer);
        project.setUpdatedAt(LocalDateTime.now());

        projectMapper.update(project);
        return ProjectResponse.from(findProjectRecord(id));
    }

    @Transactional
    public void deleteProject(Long id) {
        findProjectRecord(id);
        if (dealMapper.countByConvertedProjectId(id) > 0) {
            throw new IllegalArgumentException("This project was created from a won opportunity and cannot be deleted while the conversion link is active.");
        }
        projectMapper.deleteById(id);
    }

    public ProjectRecord findProjectRecord(Long id) {
        ProjectRecord project = projectMapper.findById(id);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id " + id);
        }
        return project;
    }

    public ProjectRecord findOrCreateProjectForMaintenance(Long customerId, String projectName, String market) {
        ProjectRecord existing = projectMapper.findByCustomerIdAndProjectName(customerId, projectName);
        if (existing != null) {
            return existing;
        }

        CustomerRecord customer = customerService.findCustomerRecord(customerId);
        ProjectRecord project = new ProjectRecord();
        project.setProjectName(projectName);
        project.setMarket(market);
        project.setLicenseAmount(java.math.BigDecimal.ZERO);
        project.setImplementationAmount(java.math.BigDecimal.ZERO);
        project.setTaxRate(java.math.BigDecimal.ZERO);
        project.setStatus(ProjectStatus.UNSIGNED_CONTRACT);
        project.setSourceDealId(null);
        project.setCustomerId(customerId);
        project.setCustomerName(customer.getName());
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        projectMapper.insert(project);
        return findProjectRecord(project.getId());
    }

    private void applyRequest(ProjectRecord project, ProjectRequest request, CustomerRecord customer) {
        project.setProjectName(request.getProjectName());
        project.setMarket(request.getMarket());
        project.setCurrency(request.getCurrency() == null ? "USD" : request.getCurrency());
        project.setLicenseAmount(request.getLicenseAmount());
        project.setImplementationAmount(request.getImplementationAmount());
        project.setTaxRate(request.getTaxRate());
        project.setStatus(request.getStatus() == null ? ProjectStatus.UNSIGNED_CONTRACT : request.getStatus());
        project.setAccountManagerId(request.getAccountManagerId());
        project.setCustomerId(customer.getId());
        project.setCustomerName(customer.getName());
    }
}
