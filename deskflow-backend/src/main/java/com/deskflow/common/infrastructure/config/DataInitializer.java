package com.deskflow.common.infrastructure.config;

import com.deskflow.employees.infrastructure.out.persistence.AllocationLimitEntity;
import com.deskflow.employees.infrastructure.out.persistence.EmployeeEntity;
import com.deskflow.employees.infrastructure.out.persistence.SpringDataEmployeeRepository;
import com.deskflow.resources.infrastructure.out.persistence.OfficeResourceEntity;
import com.deskflow.resources.infrastructure.out.persistence.SpringDataOfficeResourceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final SpringDataEmployeeRepository employeeRepo;
    private final SpringDataOfficeResourceRepository resourceRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SpringDataEmployeeRepository employeeRepo,
                           SpringDataOfficeResourceRepository resourceRepo,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.resourceRepo = resourceRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (employeeRepo.count() > 0) return;

        // Employees (test data — names and emails stay in Polish per project requirements)
        EmployeeEntity employeeOne = new EmployeeEntity();
        employeeOne.setId(UUID.randomUUID());
        employeeOne.setEmail("anna.kowalska@firma.pl");
        employeeOne.setFirstName("Anna");
        employeeOne.setLastName("Kowalska");
        employeeOne.setRole("EMPLOYEE");
        employeeOne.setPasswordHash(passwordEncoder.encode("password123"));
        addLimit(employeeOne, "DESK", 2);
        addLimit(employeeOne, "LAPTOP", 1);

        EmployeeEntity employeeTwo = new EmployeeEntity();
        employeeTwo.setId(UUID.randomUUID());
        employeeTwo.setEmail("jan.nowak@firma.pl");
        employeeTwo.setFirstName("Jan");
        employeeTwo.setLastName("Nowak");
        employeeTwo.setRole("EMPLOYEE");
        employeeTwo.setPasswordHash(passwordEncoder.encode("password123"));
        addLimit(employeeTwo, "DESK", 1);
        addLimit(employeeTwo, "CONFERENCE_ROOM", 3);

        EmployeeEntity manager = new EmployeeEntity();
        manager.setId(UUID.randomUUID());
        manager.setEmail("marek.kierownik@firma.pl");
        manager.setFirstName("Marek");
        manager.setLastName("Kierownik");
        manager.setRole("MANAGER");
        manager.setPasswordHash(passwordEncoder.encode("password123"));

        EmployeeEntity admin = new EmployeeEntity();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@firma.pl");
        admin.setFirstName("Admin");
        admin.setLastName("IT");
        admin.setRole("IT_ADMIN");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        addLimit(admin, "DESK", 5);
        addLimit(admin, "LAPTOP", 3);
        addLimit(admin, "CONFERENCE_ROOM", 5);

        employeeRepo.save(employeeOne);
        employeeRepo.save(employeeTwo);
        employeeRepo.save(manager);
        employeeRepo.save(admin);

        // Office resources (test data — names stay in Polish per project requirements)
        createResource("DESK", "Biurko A-101", "A", 1, "101");
        createResource("DESK", "Biurko A-102", "A", 1, "102");
        createResource("DESK", "Biurko B-201", "B", 2, "201");
        createResource("CONFERENCE_ROOM", "Sala Alpha", "A", 0, "S1");
        createResource("CONFERENCE_ROOM", "Sala Beta", "B", 1, "S2");
        createResource("LAPTOP", "Laptop Dell XPS", "A", 0, "IT-01");
        createResource("LAPTOP", "Laptop Lenovo ThinkPad", "A", 0, "IT-02");
        createResource("MONITOR", "Monitor Samsung 27\"", "A", 1, "M-01");
        createResource("PROJECTOR", "Projektor Epson", "A", 0, "P-01");
        createResource("VEHICLE", "Samochód VW Golf", "Parking", 0, "GAR-01");
    }

    private void addLimit(EmployeeEntity employee, String resourceType, int amount) {
        AllocationLimitEntity l = new AllocationLimitEntity();
        l.setId(UUID.randomUUID());
        l.setResourceType(resourceType);
        l.setLimitAmount(amount);
        employee.addAllocationLimit(l);
    }

    private void createResource(String type, String name, String building, int floor, String number) {
        OfficeResourceEntity resource = new OfficeResourceEntity();
        resource.setId(UUID.randomUUID());
        resource.setType(type);
        resource.setName(name);
        resource.setBuilding(building);
        resource.setFloor(floor);
        resource.setNumber(number);
        resource.setActive(true);
        resource.setAvailable(true);
        resourceRepo.save(resource);
    }
}
