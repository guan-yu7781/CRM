package com.crm.personal.crm.deal;

import org.springframework.http.HttpStatus;
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

import com.crm.personal.crm.project.ProjectResponse;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping
    public List<DealResponse> getDeals(@RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return dealService.getDealsForCustomer(customerId);
        }
        return dealService.getAllDeals();
    }

    @GetMapping("/{id}")
    public DealResponse getDeal(@PathVariable Long id) {
        return dealService.getDeal(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DealResponse createDeal(@Valid @RequestBody DealRequest request) {
        return dealService.createDeal(request);
    }

    @PutMapping("/{id}")
    public DealResponse updateDeal(@PathVariable Long id, @Valid @RequestBody DealRequest request) {
        return dealService.updateDeal(id, request);
    }

    @PostMapping("/{id}/convert-to-project")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse convertToProject(@PathVariable Long id, @Valid @RequestBody DealConversionRequest request) {
        return dealService.convertWonDealToProject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeal(@PathVariable Long id) {
        dealService.deleteDeal(id);
    }
}
