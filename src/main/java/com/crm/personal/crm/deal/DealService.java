package com.crm.personal.crm.deal;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.customer.CustomerStatus;
import com.crm.personal.crm.project.ProjectRecord;
import com.crm.personal.crm.project.ProjectResponse;
import com.crm.personal.crm.project.ProjectStatus;
import com.crm.personal.crm.project.ProjectMapper;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealService {

    private final DealMapper dealMapper;
    private final CustomerService customerService;
    private final ProjectMapper projectMapper;

    public DealService(DealMapper dealMapper, CustomerService customerService, ProjectMapper projectMapper) {
        this.dealMapper = dealMapper;
        this.customerService = customerService;
        this.projectMapper = projectMapper;
    }

    public List<DealResponse> getAllDeals(int page, int size) {
        return dealMapper.findPaged(size, page * size)
                .stream()
                .map(DealResponse::from)
                .collect(Collectors.toList());
    }

    public List<DealResponse> getDealsForCustomer(Long customerId) {
        customerService.findCustomerRecord(customerId);
        return dealMapper.findByCustomerId(customerId)
                .stream()
                .map(DealResponse::from)
                .collect(Collectors.toList());
    }

    public DealResponse getDeal(Long id) {
        return DealResponse.from(findDeal(id));
    }

    @Transactional
    public DealResponse createDeal(DealRequest request) {
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        DealRecord deal = new DealRecord();
        applyRequest(deal, request, customer);

        LocalDateTime now = LocalDateTime.now();
        deal.setCreatedAt(now);
        deal.setUpdatedAt(now);

        dealMapper.insert(deal);
        return DealResponse.from(findDeal(deal.getId()));
    }

    @Transactional
    public DealResponse updateDeal(Long id, DealRequest request) {
        DealRecord deal = findDeal(id);
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());

        applyRequest(deal, request, customer);
        deal.setUpdatedAt(LocalDateTime.now());

        dealMapper.update(deal);
        return DealResponse.from(findDeal(id));
    }

    @Transactional
    public void deleteDeal(Long id) {
        findDeal(id);
        dealMapper.deleteById(id);
    }

    @Transactional
    public ProjectResponse convertWonDealToProject(Long id, DealConversionRequest request) {
        DealRecord deal = findDeal(id);
        if (deal.getStage() != DealStage.WON) {
            throw new IllegalArgumentException("Only won opportunities can be converted into a project.");
        }
        if (deal.getConvertedProjectId() != null) {
            throw new IllegalArgumentException("This opportunity has already been converted into a project.");
        }

        CustomerRecord customer = customerService.findCustomerRecord(deal.getCustomerId());
        ProjectRecord project = new ProjectRecord();
        project.setProjectName(request.getProjectName());
        project.setMarket(request.getMarket());
        project.setLicenseAmount(defaultAmount(request.getLicenseAmount()));
        project.setImplementationAmount(defaultAmount(request.getImplementationAmount()));
        project.setTaxRate(defaultAmount(request.getTaxRate()));
        project.setStatus(ProjectStatus.SIGNED_CONTRACT);
        project.setSourceDealId(deal.getId());
        project.setCustomerId(customer.getId());
        project.setCustomerName(customer.getName());
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        projectMapper.insert(project);

        int updated = dealMapper.updateConversion(deal.getId(), project.getId(), now, now);
        if (updated == 0) {
            throw new IllegalStateException("The opportunity conversion could not be persisted.");
        }

        return ProjectResponse.from(projectMapper.findById(project.getId()));
    }

    public DealRecord findDealRecord(Long id) {
        return findDeal(id);
    }

    private DealRecord findDeal(Long id) {
        DealRecord deal = dealMapper.findById(id);
        if (deal == null) {
            throw new ResourceNotFoundException("Deal not found with id " + id);
        }
        return deal;
    }

    private void applyRequest(DealRecord deal, DealRequest request, CustomerRecord customer) {
        deal.setTitle(request.getTitle());
        deal.setAmount(request.getAmount());
        deal.setCurrency(request.getCurrency() == null ? "USD" : request.getCurrency());
        deal.setStage(request.getStage() == null ? DealStage.NEW : request.getStage());
        // Use explicit type if provided; otherwise derive from customer status
        if (request.getOpportunityType() != null) {
            deal.setOpportunityType(request.getOpportunityType());
        } else {
            deal.setOpportunityType(
                CustomerStatus.ACTIVE.equals(customer.getStatus())
                    ? OpportunityType.EXPANSION
                    : OpportunityType.ACQUISITION
            );
        }
        deal.setMarket(request.getMarket());
        deal.setExpectedCloseDate(request.getExpectedCloseDate());
        deal.setNotes(request.getNotes());
        deal.setCustomerId(customer.getId());
        deal.setCustomerName(customer.getName());
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
