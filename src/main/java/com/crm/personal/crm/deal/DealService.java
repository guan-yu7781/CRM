package com.crm.personal.crm.deal;

import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private final CustomerService customerService;

    public DealService(DealRepository dealRepository, CustomerService customerService) {
        this.dealRepository = dealRepository;
        this.customerService = customerService;
    }

    public List<DealResponse> getAllDeals() {
        return dealRepository.findAll()
                .stream()
                .map(DealResponse::from)
                .collect(Collectors.toList());
    }

    public List<DealResponse> getDealsForCustomer(Long customerId) {
        customerService.findCustomer(customerId);
        return dealRepository.findByCustomerId(customerId)
                .stream()
                .map(DealResponse::from)
                .collect(Collectors.toList());
    }

    public DealResponse getDeal(Long id) {
        return DealResponse.from(findDeal(id));
    }

    @Transactional
    public DealResponse createDeal(DealRequest request) {
        Customer customer = customerService.findCustomer(request.getCustomerId());

        Deal deal = new Deal();
        applyRequest(deal, request, customer);

        LocalDateTime now = LocalDateTime.now();
        deal.setCreatedAt(now);
        deal.setUpdatedAt(now);

        return DealResponse.from(dealRepository.save(deal));
    }

    @Transactional
    public DealResponse updateDeal(Long id, DealRequest request) {
        Deal deal = findDeal(id);
        Customer customer = customerService.findCustomer(request.getCustomerId());

        applyRequest(deal, request, customer);
        deal.setUpdatedAt(LocalDateTime.now());

        return DealResponse.from(dealRepository.save(deal));
    }

    @Transactional
    public void deleteDeal(Long id) {
        Deal deal = findDeal(id);
        dealRepository.delete(deal);
    }

    private Deal findDeal(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found with id " + id));
    }

    private void applyRequest(Deal deal, DealRequest request, Customer customer) {
        deal.setTitle(request.getTitle());
        deal.setAmount(request.getAmount());
        deal.setStage(request.getStage() == null ? DealStage.NEW : request.getStage());
        deal.setExpectedCloseDate(request.getExpectedCloseDate());
        deal.setNotes(request.getNotes());
        deal.setCustomer(customer);
    }
}
