package com.crm.personal.crm.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/access-control")
public class AccessControlController {

    private final AccessControlService accessControlService;

    public AccessControlController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_VIEW')")
    public List<AccessControlRoleResponse> getRoles() {
        return accessControlService.getRoles();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_VIEW')")
    public List<AccessControlUserResponse> getUsers() {
        return accessControlService.getUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_MANAGE')")
    public AccessControlUserResponse createUser(@Valid @RequestBody AccessControlUserRequest request) {
        return accessControlService.createUser(request);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ACCESS_CONTROL_MANAGE')")
    public AccessControlUserResponse updateUser(@PathVariable Long id,
                                                @Valid @RequestBody AccessControlUserRequest request) {
        return accessControlService.updateUser(id, request);
    }
}
