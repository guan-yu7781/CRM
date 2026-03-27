package com.crm.personal.crm.activity;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACTIVITY_VIEW')")
    public List<ActivityResponse> getActivities(@RequestParam(required = false) Long customerId) {
        return activityService.getActivities(customerId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_VIEW')")
    public ActivityResponse getActivity(@PathVariable Long id) {
        return activityService.getActivity(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ACTIVITY_CREATE')")
    public ActivityResponse createActivity(@Valid @RequestBody ActivityRequest request, Authentication authentication) {
        return activityService.createActivity(request, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTIVITY_EDIT')")
    public ActivityResponse updateActivity(@PathVariable Long id,
                                           @Valid @RequestBody ActivityRequest request,
                                           Authentication authentication) {
        return activityService.updateActivity(id, request, authentication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ACTIVITY_DELETE')")
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }
}
