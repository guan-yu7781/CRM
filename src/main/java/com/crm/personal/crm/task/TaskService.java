package com.crm.personal.crm.task;

import com.crm.personal.crm.customer.CustomerRecord;
import com.crm.personal.crm.customer.CustomerService;
import com.crm.personal.crm.deal.DealRecord;
import com.crm.personal.crm.deal.DealService;
import com.crm.personal.crm.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final CustomerService customerService;
    private final DealService dealService;

    public TaskService(TaskMapper taskMapper, CustomerService customerService, DealService dealService) {
        this.taskMapper = taskMapper;
        this.customerService = customerService;
        this.dealService = dealService;
    }

    public List<TaskResponse> getTasks(Long customerId, TaskStatus status) {
        List<TaskRecord> tasks;
        if (customerId != null) {
            customerService.findCustomerRecord(customerId);
            tasks = taskMapper.findByCustomerId(customerId);
        } else if (status != null) {
            tasks = taskMapper.findByStatus(status);
        } else {
            tasks = taskMapper.findAll();
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
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());
        DealRecord deal = resolveDeal(request.getDealId(), customer.getId());

        TaskRecord task = new TaskRecord();
        applyRequest(task, request, customer, deal);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        taskMapper.insert(task);
        return TaskResponse.from(findTask(task.getId()));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        TaskRecord task = findTask(id);
        CustomerRecord customer = customerService.findCustomerRecord(request.getCustomerId());
        DealRecord deal = resolveDeal(request.getDealId(), customer.getId());

        applyRequest(task, request, customer, deal);
        task.setUpdatedAt(LocalDateTime.now());

        taskMapper.update(task);
        return TaskResponse.from(findTask(id));
    }

    @Transactional
    public void deleteTask(Long id) {
        findTask(id);
        taskMapper.deleteById(id);
    }

    private TaskRecord findTask(Long id) {
        TaskRecord task = taskMapper.findById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found with id " + id);
        }
        return task;
    }

    private DealRecord resolveDeal(Long dealId, Long customerId) {
        if (dealId == null) {
            return null;
        }

        DealRecord deal = dealService.findDealRecord(dealId);

        if (!deal.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Selected deal does not belong to the provided customer");
        }
        return deal;
    }

    private void applyRequest(TaskRecord task, TaskRequest request, CustomerRecord customer, DealRecord deal) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() == null ? TaskStatus.OPEN : request.getStatus());
        task.setPriority(request.getPriority() == null ? TaskPriority.MEDIUM : request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setCustomerId(customer.getId());
        task.setCustomerName(customer.getName());
        if (deal != null) {
            task.setDealId(deal.getId());
            task.setDealTitle(deal.getTitle());
        } else {
            task.setDealId(null);
            task.setDealTitle(null);
        }
    }
}
