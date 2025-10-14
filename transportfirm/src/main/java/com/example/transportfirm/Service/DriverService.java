package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Repository.DriverRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DriverService {
    private final DriverRepository repository;

    public DriverService(DriverRepository repository) {
        this.repository = repository;
    }

    public List<DriverInfo> getAll() { return repository.findAll(); }

    public DriverInfo getByEgn(String egn) {
        return repository.findById(egn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }
    public DriverInfo save(DriverInfo driver) { return repository.save(driver); }

    public DriverInfo update(String egn, DriverInfo driver) {
        if (!repository.existsById(egn))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
        driver.setEgn(egn);
        return repository.save(driver);
    }
    public void delete(String egn) {
        if (!repository.existsById(egn))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
        repository.deleteById(egn);
    }

}
