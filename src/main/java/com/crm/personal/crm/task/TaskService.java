package com.crm.personal.crm.task;

import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.deal.Deal;
import com.crm.personal.crm.deal.DealRepository;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CustomerService customerService;
    private final DealRepository dealRepository;

    public TaskService(TaskRepository taskRepository, CustomerService customerService, DealRepository dealRepository) {
        this.taskRepository = taskRepository;
        this.customerService = customerService;
        this.dealRepository = dealRepository;
    }

    public List<TaskResponse> getTasks(Long customerId, TaskStatus status) {
        List<CrmTask> tasks;
        if (customerId != null) {
            customerService.findCustomer(customerId);
            tasks = taskRepository.findByCustomerId(customerId);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    public TaskResponse getTask(Long id) {
        return TaskResponse.from(findTask(id));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Deal deal = resolveDeal(request.getDealId(), customer.getId());

        CrmTask task = new CrmTask();
        applyRequest(task, request, customer, deal);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        CrmTask task = findTask(id);
        Customer customer = customerService.findCustomer(request.getCustomerId());
        Deal deal = resolveDeal(request.getDealId(), customer.getId());

        applyRequest(task, request, customer, deal);
        task.setUpdatedAt(LocalDateTime.now());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        CrmTask task = findTask(id);
        taskRepository.delete(task);
    }

    private CrmTask findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
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

    private void applyRequest(CrmTask task, TaskRequest request, Customer customer, Deal deal) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() == null ? TaskStatus.OPEN : request.getStatus());
        task.setPriority(request.getPriority() == null ? TaskPriority.MEDIUM : request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setCustomer(customer);
        task.setDeal(deal);
    }
}
