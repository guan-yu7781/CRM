package com.crm.personal.crm.project;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
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

    public ProjectService(ProjectMapper projectMapper, CustomerService customerService) {
        this.projectMapper = projectMapper;
        this.customerService = customerService;
    }

    public List<ProjectResponse> getProjects(Long customerId) {
        List<ProjectRecord> projects = customerId == null
                ? projectMapper.findAll()
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
        project.setAmount(java.math.BigDecimal.ZERO);
        project.setTaxRate(java.math.BigDecimal.ZERO);
        project.setStatus(ProjectStatus.IN_PROGRESS);
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
        project.setAmount(request.getAmount());
        project.setTaxRate(request.getTaxRate());
        project.setStatus(request.getStatus() == null ? ProjectStatus.IN_PROGRESS : request.getStatus());
        project.setCustomerId(customer.getId());
        project.setCustomerName(customer.getName());
    }
}
