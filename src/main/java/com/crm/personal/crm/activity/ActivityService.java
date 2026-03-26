package com.crm.personal.crm.activity;

import com.crm.personal.crm.contact.Contact;
import com.crm.personal.crm.contact.ContactRepository;
import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.deal.Deal;
import com.crm.personal.crm.deal.DealRepository;
import com.crm.personal.crm.security.AppUser;
import com.crm.personal.crm.security.AppUserRepository;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CustomerService customerService;
    private final ContactRepository contactRepository;
    private final DealRepository dealRepository;
    private final AppUserRepository appUserRepository;

    public ActivityService(ActivityRepository activityRepository,
                           CustomerService customerService,
                           ContactRepository contactRepository,
                           DealRepository dealRepository,
                           AppUserRepository appUserRepository) {
        this.activityRepository = activityRepository;
        this.customerService = customerService;
        this.contactRepository = contactRepository;
        this.dealRepository = dealRepository;
        this.appUserRepository = appUserRepository;
    }

    public List<ActivityResponse> getActivities(Long customerId) {
        List<Activity> activities = customerId == null
                ? activityRepository.findAll()
                : activityRepository.findByCustomerIdOrderByActivityDateDesc(customerId);

        return activities.stream()
                .map(ActivityResponse::from)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivity(Long id) {
        return ActivityResponse.from(findActivity(id));
    }

    @Transactional
    public ActivityResponse createActivity(ActivityRequest request, Authentication authentication) {
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Contact contact = resolveContact(request.getContactId(), customer.getId());
        Deal deal = resolveDeal(request.getDealId(), customer.getId());
        AppUser user = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Activity activity = new Activity();
        activity.setType(request.getType());
        activity.setSubject(request.getSubject());
        activity.setDetails(request.getDetails());
        activity.setActivityDate(request.getActivityDate());
        activity.setCustomer(customer);
        activity.setContact(contact);
        activity.setDeal(deal);
        activity.setCreatedBy(user);
        activity.setCreatedAt(LocalDateTime.now());

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, ActivityRequest request, Authentication authentication) {
        Activity activity = findActivity(id);
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Contact contact = resolveContact(request.getContactId(), customer.getId());
        Deal deal = resolveDeal(request.getDealId(), customer.getId());
        AppUser user = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        activity.setType(request.getType());
        activity.setSubject(request.getSubject());
        activity.setDetails(request.getDetails());
        activity.setActivityDate(request.getActivityDate());
        activity.setCustomer(customer);
        activity.setContact(contact);
        activity.setDeal(deal);
        activity.setCreatedBy(user);

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(Long id) {
        activityRepository.delete(findActivity(id));
    }

    private Activity findActivity(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id " + id));
    }

    private Contact resolveContact(Long contactId, Long customerId) {
        if (contactId == null) {
            return null;
        }

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + contactId));

        if (!contact.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Selected contact does not belong to the provided customer");
        }
        return contact;
    }

    private Deal resolveDeal(Long dealId, Long customerId) {
        if (dealId == null) {
            return null;
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new ResourceNotFoundException("Deal not found with id " + dealId));

        if (!deal.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Selected deal does not belong to the provided customer");
        }
        return deal;
    }
}
