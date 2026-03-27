package com.crm.personal.crm.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserRole effectiveRole = user.getRole().getEffectiveRole();

        return new User(
                user.getUsername(),
                user.getPassword(),
                Stream.concat(
                                Stream.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole.name())),
                                effectiveRole.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.name()))
                        )
                        .collect(Collectors.toList())
        );
    }
}
