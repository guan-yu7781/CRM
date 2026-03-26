package com.crm.personal.crm.activity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "contact", "deal", "createdBy"})
    List<Activity> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "contact", "deal", "createdBy"})
    Optional<Activity> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "contact", "deal", "createdBy"})
    List<Activity> findByCustomerIdOrderByActivityDateDesc(Long customerId);
}
