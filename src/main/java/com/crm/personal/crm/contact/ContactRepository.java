package com.crm.personal.crm.contact;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<Contact> findAll();

    @Override
    @EntityGraph(attributePaths = "customer")
    Optional<Contact> findById(Long id);

    @EntityGraph(attributePaths = "customer")
    List<Contact> findByCustomerId(Long customerId);
}
