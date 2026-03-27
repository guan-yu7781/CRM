package com.crm.personal.crm.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toAuthResponse(user);
    }

    public AuthResponse currentUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toAuthResponse(user);
    }

    @PostConstruct
    public void seedDefaultUser() {
        seedUserIfMissing("admin", "System Administrator", UserRole.SUPER_ADMIN);
        seedUserIfMissing("crmadmin", "CRM Administrator", UserRole.CRM_ADMIN);
        seedUserIfMissing("sales.manager", "Sales Manager", UserRole.SALES_MANAGER);
        seedUserIfMissing("rm.gary", "Relationship Manager", UserRole.RELATIONSHIP_MANAGER);
        seedUserIfMissing("finance.officer", "Finance Officer", UserRole.FINANCE_OFFICER);
    }

    private void seedUserIfMissing(String username, String fullName, UserRole role) {
        if (appUserRepository.findByUsername(username).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        appUserRepository.save(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        UserRole effectiveRole = user.getRole().getEffectiveRole();
        List<String> permissions = effectiveRole.getPermissions().stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        return new AuthResponse(
                jwtService.generateToken(user),
                user.getUsername(),
                effectiveRole.name(),
                effectiveRole.getLabel(),
                effectiveRole.getDataScope().name(),
                permissions
        );
    }
}
