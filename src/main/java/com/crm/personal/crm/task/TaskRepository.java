package com.crm.personal.crm.task;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<CrmTask, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "deal"})
    List<CrmTask> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "deal"})
    Optional<CrmTask> findById(Long id);

    @EntityGraph(attributePaths = {"customer", "deal"})
    List<CrmTask> findByCustomerId(Long customerId);

    @EntityGraph(attributePaths = {"customer", "deal"})
    List<CrmTask> findByStatus(TaskStatus status);
}
