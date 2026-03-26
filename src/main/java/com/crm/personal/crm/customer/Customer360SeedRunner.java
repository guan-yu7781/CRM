package com.crm.personal.crm.customer;

import com.crm.personal.crm.activity.Activity;
import com.crm.personal.crm.activity.ActivityRepository;
import com.crm.personal.crm.activity.ActivityType;
import com.crm.personal.crm.contact.Contact;
import com.crm.personal.crm.contact.ContactMapper;
import com.crm.personal.crm.contact.ContactRecord;
import com.crm.personal.crm.contact.ContactRepository;
import com.crm.personal.crm.customer.Customer;
import com.crm.personal.crm.deal.Deal;
import com.crm.personal.crm.deal.DealMapper;
import com.crm.personal.crm.deal.DealRecord;
import com.crm.personal.crm.deal.DealRepository;
import com.crm.personal.crm.deal.DealStage;
import com.crm.personal.crm.security.AppUser;
import com.crm.personal.crm.security.AppUserRepository;
import com.crm.personal.crm.task.TaskMapper;
import com.crm.personal.crm.task.TaskPriority;
import com.crm.personal.crm.task.TaskRecord;
import com.crm.personal.crm.task.TaskStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class Customer360SeedRunner implements ApplicationRunner {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final ContactMapper contactMapper;
    private final ContactRepository contactRepository;
    private final DealMapper dealMapper;
    private final DealRepository dealRepository;
    private final TaskMapper taskMapper;
    private final ActivityRepository activityRepository;
    private final AppUserRepository appUserRepository;

    public Customer360SeedRunner(CustomerMapper customerMapper,
                                 CustomerRepository customerRepository,
                                 ContactMapper contactMapper,
                                 ContactRepository contactRepository,
                                 DealMapper dealMapper,
                                 DealRepository dealRepository,
                                 TaskMapper taskMapper,
                                 ActivityRepository activityRepository,
                                 AppUserRepository appUserRepository) {
        this.customerMapper = customerMapper;
        this.customerRepository = customerRepository;
        this.contactMapper = contactMapper;
        this.contactRepository = contactRepository;
        this.dealMapper = dealMapper;
        this.dealRepository = dealRepository;
        this.taskMapper = taskMapper;
        this.activityRepository = activityRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        CustomerRecord customer = customerMapper.findAll().stream()
                .filter(item -> "Gary".equalsIgnoreCase(item.getName()))
                .findFirst()
                .orElseGet(customerMapper::findFirst);
        if (customer == null) {
            return;
        }

        seedContacts(customer);
        seedDeals(customer);
        seedTasks(customer);
        seedActivities(customer);
    }

    private void seedContacts(CustomerRecord customer) {
        List<ContactRecord> contacts = contactMapper.findByCustomerId(customer.getId());
        seedContactIfMissing(contacts, customer, "Mary", "Wanjiku", "mary.wanjiku@ncba.co.ke", "+254700110220",
                "Head of Digital Channels", "Executive sponsor for the multi-year service relationship.");
        seedContactIfMissing(contacts, customer, "Daniel", "Mwangi", "daniel.mwangi@ncba.co.ke", "+254700330440",
                "Programme Director", "Primary delivery counterpart for core banking and channel initiatives.");
        seedContactIfMissing(contacts, customer, "Grace", "Otieno", "grace.otieno@ncba.co.ke", "+254700550660",
                "Procurement Lead", "Commercial and renewal coordination contact.");
    }

    private void seedDeals(CustomerRecord customer) {
        List<DealRecord> deals = dealMapper.findByCustomerId(customer.getId());
        seedDealIfMissing(deals, customer, "Regional Mobile Lending Expansion", new BigDecimal("425000.00"),
                DealStage.NEGOTIATION, LocalDate.now().plusDays(45),
                "Expansion opportunity covering Tanzania and Uganda digital lending capabilities.");
        seedDealIfMissing(deals, customer, "Customer 360 Analytics Rollout", new BigDecimal("160000.00"),
                DealStage.PROPOSAL_SENT, LocalDate.now().plusDays(30),
                "Phase-two analytics workstream for executive dashboards and engagement insights.");
    }

    private void seedTasks(CustomerRecord customer) {
        List<TaskRecord> tasks = taskMapper.findByCustomerId(customer.getId());
        DealRecord primaryDeal = dealMapper.findByCustomerId(customer.getId()).stream()
                .max(Comparator.comparing(DealRecord::getAmount))
                .orElse(null);

        seedTaskIfMissing(tasks, customer, primaryDeal, "Prepare quarterly service review pack",
                "Compile delivery KPIs, issue log, maintenance exposure, and executive talking points.",
                TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.now().plusDays(3));
        seedTaskIfMissing(tasks, customer, primaryDeal, "Confirm renewal commercials with procurement",
                "Align renewal pricing, tax assumptions, and contract scope ahead of formal submission.",
                TaskStatus.OPEN, TaskPriority.HIGH, LocalDate.now().plusDays(10));
        seedTaskIfMissing(tasks, customer, null, "Validate open action tracker with programme office",
                "Reconcile implementation dependencies and confirm owners for the next steering committee.",
                TaskStatus.OPEN, TaskPriority.MEDIUM, LocalDate.now().plusDays(7));
    }

    private void seedActivities(CustomerRecord customer) {
        if (!activityRepository.findByCustomerIdOrderByActivityDateDesc(customer.getId()).isEmpty()) {
            return;
        }

        Customer managedCustomer = customerRepository.findById(customer.getId()).orElse(null);
        AppUser admin = appUserRepository.findByUsername("admin").orElse(null);
        if (managedCustomer == null || admin == null) {
            return;
        }

        Contact primaryContact = contactRepository.findByCustomerId(customer.getId()).stream().findFirst().orElse(null);
        Deal primaryDeal = dealRepository.findByCustomerId(customer.getId()).stream().findFirst().orElse(null);

        activityRepository.save(buildActivity(
                ActivityType.MEETING,
                "Executive account review completed",
                "Reviewed service stability, maintenance posture, and pipeline conversion priorities with the customer leadership team.",
                LocalDateTime.now().minusDays(4),
                managedCustomer,
                primaryContact,
                primaryDeal,
                admin
        ));
        activityRepository.save(buildActivity(
                ActivityType.FOLLOW_UP,
                "Renewal scope clarification sent",
                "Shared updated scope notes, delivery assumptions, and commercial dependencies for the annual maintenance renewal.",
                LocalDateTime.now().minusDays(2),
                managedCustomer,
                primaryContact,
                primaryDeal,
                admin
        ));
        activityRepository.save(buildActivity(
                ActivityType.CALL,
                "Programme risk checkpoint",
                "Discussed implementation sequencing, open actions, and decision timeline for the regional rollout opportunity.",
                LocalDateTime.now().minusHours(28),
                managedCustomer,
                primaryContact,
                primaryDeal,
                admin
        ));
    }

    private void seedContactIfMissing(List<ContactRecord> existing,
                                      CustomerRecord customer,
                                      String firstName,
                                      String lastName,
                                      String email,
                                      String phone,
                                      String jobTitle,
                                      String notes) {
        boolean present = existing.stream().anyMatch(item -> email.equalsIgnoreCase(item.getEmail()));
        if (present) {
            return;
        }

        ContactRecord record = new ContactRecord();
        record.setFirstName(firstName);
        record.setLastName(lastName);
        record.setEmail(email);
        record.setPhone(phone);
        record.setJobTitle(jobTitle);
        record.setNotes(notes);
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        contactMapper.insert(record);
    }

    private void seedDealIfMissing(List<DealRecord> existing,
                                   CustomerRecord customer,
                                   String title,
                                   BigDecimal amount,
                                   DealStage stage,
                                   LocalDate expectedCloseDate,
                                   String notes) {
        boolean present = existing.stream().anyMatch(item -> title.equalsIgnoreCase(item.getTitle()));
        if (present) {
            return;
        }

        DealRecord record = new DealRecord();
        record.setTitle(title);
        record.setAmount(amount);
        record.setStage(stage);
        record.setExpectedCloseDate(expectedCloseDate);
        record.setNotes(notes);
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        dealMapper.insert(record);
    }

    private void seedTaskIfMissing(List<TaskRecord> existing,
                                   CustomerRecord customer,
                                   DealRecord deal,
                                   String title,
                                   String description,
                                   TaskStatus status,
                                   TaskPriority priority,
                                   LocalDate dueDate) {
        boolean present = existing.stream().anyMatch(item -> title.equalsIgnoreCase(item.getTitle()));
        if (present) {
            return;
        }

        TaskRecord record = new TaskRecord();
        record.setTitle(title);
        record.setDescription(description);
        record.setStatus(status);
        record.setPriority(priority);
        record.setDueDate(dueDate);
        record.setCustomerId(customer.getId());
        record.setCustomerName(customer.getName());
        if (deal != null) {
            record.setDealId(deal.getId());
            record.setDealTitle(deal.getTitle());
        }
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        taskMapper.insert(record);
    }

    private Activity buildActivity(ActivityType type,
                                   String subject,
                                   String details,
                                   LocalDateTime activityDate,
                                   Customer customer,
                                   Contact contact,
                                   Deal deal,
                                   AppUser createdBy) {
        Activity activity = new Activity();
        activity.setType(type);
        activity.setSubject(subject);
        activity.setDetails(details);
        activity.setActivityDate(activityDate);
        activity.setCustomer(customer);
        activity.setContact(contact);
        activity.setDeal(deal);
        activity.setCreatedBy(createdBy);
        activity.setCreatedAt(LocalDateTime.now());
        return activity;
    }
}
