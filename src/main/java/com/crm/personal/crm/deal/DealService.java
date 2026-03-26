package com.crm.personal.crm.deal;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealService {

    private final DealMapper dealMapper;
    private final CustomerService customerService;

    public DealService(DealMapper dealMapper, CustomerService customerService) {
        this.dealMapper = dealMapper;
        this.customerService = customerService;
    }

    public List<DealResponse> getAllDeals() {
        return dealMapper.findAll()
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
        deal.setStage(request.getStage() == null ? DealStage.NEW : request.getStage());
        deal.setExpectedCloseDate(request.getExpectedCloseDate());
        deal.setNotes(request.getNotes());
        deal.setCustomerId(customer.getId());
        deal.setCustomerName(customer.getName());
    }
}
