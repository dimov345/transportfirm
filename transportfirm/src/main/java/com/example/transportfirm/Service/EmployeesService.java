package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.*;
import com.example.transportfirm.Repository.EmployeeRepository;
import com.example.transportfirm.Repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class EmployeesService {

    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;

    public EmployeesService(EmployeeRepository employeeRepository,
                           VehicleRepository vehicleRepository) {
        this.employeeRepository = employeeRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // =============================
    // CRUD за всички служители
    // =============================

    public List<Employees> getAll() {
        return employeeRepository.findAll();
    }

    public List<Employees> getByRole(Role role) {
        return employeeRepository.findByRole(role);
    }

    public Employees getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    public Employees create(Employees employee) {
        if (employee.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be specified");
        }

        // Създаваме конкретен подтип според ролята
        if (employee.getRole() == Role.MECHANIC && !(employee instanceof Mechanic)) {
            Mechanic mechanic = new Mechanic();
            copyBaseFields(employee, mechanic);
            return employeeRepository.save(mechanic);
        } else if (employee.getRole() == Role.SPEDITOR && !(employee instanceof Speditor)) {
            Speditor speditor = new Speditor();
            copyBaseFields(employee, speditor);
            return employeeRepository.save(speditor);
        }

        return employeeRepository.save(employee);
    }

    private void copyBaseFields(Employees source, Employees target) {
        target.setEgn(source.getEgn());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setPhone(source.getPhone());
        target.setEmail(source.getEmail());
        target.setAddress(source.getAddress());
        target.setHireDate(source.getHireDate());
        target.setSalary(source.getSalary());
        target.setRole(source.getRole());
        target.setActive(source.isActive());
    }


    public Employees update(Long id, Employees updated) {
        Employees existing = getById(id);
        updated.setId(existing.getId());
        return employeeRepository.save(updated);
    }

    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    // =============================
    // Спедитор: назначаване на камиони
    // =============================

    public Speditor assignVehiclesToSpeditor(Long speditorId, List<String> plateNumbers) {
        Employees emp = getById(speditorId);
        if (!(emp instanceof Speditor speditor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is not a Speditor");
        }

        List<VehicleRecord> vehicles = vehicleRepository.findAllById(plateNumbers);
        speditor.setVehicles(vehicles);
        return employeeRepository.save(speditor);
    }

    // =============================
    // Монтьор: назначаване на камиони
    // =============================

    public Mechanic assignVehiclesToMechanic(Long mechanicId, List<String> plateNumbers) {
        Employees emp = getById(mechanicId);
        if (!(emp instanceof Mechanic mechanic)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is not a Mechanic");
        }

        List<VehicleRecord> vehicles = vehicleRepository.findAllById(plateNumbers);
        mechanic.setVehicles(vehicles);
        return employeeRepository.save(mechanic);
    }
}
