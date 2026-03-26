package com.crm.personal.crm.deal;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<Deal> findAll();

    @EntityGraph(attributePaths = "customer")
    List<Deal> findByCustomerId(Long customerId);

    @Override
    @EntityGraph(attributePaths = "customer")
    Optional<Deal> findById(Long id);
}
