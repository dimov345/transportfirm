package com.example.transportfirm.service.employee;

import com.example.transportfirm.entity.DispatcherInfo;
import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.repository.dispatcher.DispatcherRepository;
import com.example.transportfirm.repository.driver.DriverDocumentRepository;
import com.example.transportfirm.repository.driver.DriverRepository;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.repository.mechanic.MechanicRepository;
import com.example.transportfirm.repository.vehicle.TruckGroupRepository;
import com.example.transportfirm.repository.auth.UserRepository;
import com.example.transportfirm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DriverRepository driverInfoRepository;
    private final MechanicRepository  mechanicRepository;
    private final DispatcherRepository dispatcherRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final TruckGroupRepository truckGroupRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public Employee createEmployee(Employee employee) {
        Employee saved = employeeRepository.save(employee);

        switch (saved.getJobTitle()) {
            case DRIVER -> {
                DriverInfo info = new DriverInfo();
                info.setEmployee(saved);
                driverInfoRepository.save(info);
            }
            case MECHANIC -> {
                MechanicInfo info = new MechanicInfo();
                info.setEmployee(saved);
                mechanicRepository.save(info);
            }
            case DISPATCHER -> {
                DispatcherInfo info = new DispatcherInfo();
                info.setEmployee(saved);
                dispatcherRepository.save(info);
            }
        }

        return saved;
    }

    @Transactional
    public Employee update(UUID id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Забрана за промяна на jobTitle
        if (updated.getJobTitle() != null && existing.getJobTitle() != updated.getJobTitle()) {
            throw new RuntimeException("Job title cannot be changed after creation");
        }

        // Лични данни
        existing.setEgn(updated.getEgn());
        existing.setName(updated.getName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setPhone(updated.getPhone());

        String oldEmail = existing.getEmail();
        String newEmail = updated.getEmail();
        existing.setEmail(newEmail);
        if (newEmail != null && !newEmail.equals(oldEmail)) {
            userRepository.findByEmail(oldEmail).ifPresent(u -> {
                u.setEmail(newEmail);
                userRepository.save(u);
            });
        }

        // Адреси
        existing.setAddressPermanent(updated.getAddressPermanent());
        existing.setAddressCurrent(updated.getAddressCurrent());

        // Фирмени данни (БЕЗ jobTitle)
        existing.setHiredDate(updated.getHiredDate());
        existing.setEmploymentStatus(updated.getEmploymentStatus());
        existing.setFiredDate(updated.getFiredDate());
        existing.setContractType(updated.getContractType());
        existing.setSalary(updated.getSalary());
        existing.setSalaryNeto(updated.getSalaryNeto());        // каза, че си го оправил, но го оставям тук
        existing.setSalaryCurrency(updated.getSalaryCurrency()); // и това
        existing.setWorkingHours(updated.getWorkingHours());

        // Банкови данни
        existing.setBankName(updated.getBankName());
        existing.setIban(updated.getIban());

        // Достъп
        existing.setRole(updated.getRole());

        return employeeRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        switch (employee.getJobTitle()) {
            case DRIVER -> {
                driverInfoRepository.findByEmployeeId(id).ifPresent(driverInfo -> {
                    // изтриваме driver документите от файловата система
                    List<DriverDocument> driverDocs = driverDocumentRepository.findByEmployee_Id(id);
                    driverDocs.forEach(doc -> {
                        try {
                            Files.deleteIfExists(Path.of(doc.getFilePath()));
                        } catch (IOException ignored) {}
                    });
                    driverDocumentRepository.deleteAll(driverDocs);

                    // откачаме vehicle-то от driver-а преди изтриване
                    if (driverInfo.getVehicle() != null) {
                        driverInfo.setVehicle(null);
                        driverInfoRepository.save(driverInfo);
                    }

                    driverInfoRepository.delete(driverInfo);
                });
            }
            case MECHANIC -> {
                mechanicRepository.findByEmployee_Id(id).ifPresent(mechanicInfo -> {
                    List<TruckGroup> groups = truckGroupRepository.findByMechanic_Id(mechanicInfo.getId());
                    groups.forEach(group -> {
                        // null-ваме mechanic референцията в групата и null-ваме vehicle assignments
                        group.getMechanicVehicles().forEach(v -> {
                            v.setMechanicGroup(null);
                            vehicleRepository.save(v);
                        });
                        truckGroupRepository.delete(group);
                    });
                    mechanicRepository.delete(mechanicInfo);
                });
            }
            case DISPATCHER -> {
                dispatcherRepository.findByEmployee_Id(id).ifPresent(dispatcherInfo -> {
                    List<TruckGroup> groups = truckGroupRepository.findByDispatcher_Id(dispatcherInfo.getId());
                    groups.forEach(group -> {
                        // null-ваме dispatcher референцията в групата и null-ваме vehicle assignments
                        group.getDispatcherVehicles().forEach(v -> {
                            v.setDispatcherGroup(null);
                            vehicleRepository.save(v);
                        });
                        truckGroupRepository.delete(group);
                    });
                    dispatcherRepository.delete(dispatcherInfo);
                });
            }
        }

        employeeRepository.delete(employee);
    }

    @Transactional(readOnly = true)
    public Employee getById(UUID id) {
        return employeeRepository.findByIdWithInfos(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Transactional(readOnly = true)
    public List<Employee> getAll() {
        return employeeRepository.findAllWithInfos();
    }

    @Transactional(readOnly = true)
    public List<Employee> getByJobTitle(JobTitle jobTitle) {
        return employeeRepository.findByJobTitleWithInfos(jobTitle);
    }

    @Transactional(readOnly = true)
    public Employee getByEgn(String egn) {
        return employeeRepository.findByEgn(egn)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // --- Paginated queries ---

    @Transactional(readOnly = true)
    public Page<Employee> getDriversPaged(String search, String status, int expiryDays, int page, int size) {
        int days = expiryDays > 0 ? expiryDays : 30;
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return employeeRepository.searchDriversWithStatus(
                JobTitle.DRIVER,
                search != null ? search.trim() : "",
                status != null ? status.trim() : "",
                expiryDate,
                PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    @Transactional(readOnly = true)
    public Page<Employee> getByJobTitlePaged(JobTitle jobTitle, String search, int page, int size) {
        return employeeRepository.searchByJobTitle(
                jobTitle,
                search != null ? search.trim() : "",
                PageRequest.of(page, size, Sort.by("name").ascending()));
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllPaged(String search, int page, int size) {
        return employeeRepository.searchAll(
                search != null ? search.trim() : "",
                PageRequest.of(page, size, Sort.by("name").ascending()));
    }
}
