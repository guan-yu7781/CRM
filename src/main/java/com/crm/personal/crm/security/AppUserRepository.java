package com.crm.personal.crm.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findAllByOrderByCreatedAtAscUsernameAsc();

    List<AppUser> findByRoleInOrderByFullNameAsc(List<UserRole> roles);

    List<AppUser> findByEmailIsNotNullAndEmailIsNotOrderByFullNameAsc(String empty);
}
