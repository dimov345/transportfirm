package com.example.transportfirm.service.driver;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.repository.driver.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public DriverInfo getById(UUID driverInfoId) {
        return driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));
    }

    @Transactional(readOnly = true)
    public DriverInfo getByEmployeeId(UUID employeeId) {
        return driverRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found for employee"));
    }

    @Transactional
    public DriverInfo update(UUID driverInfoId, DriverInfo updated) {
        DriverInfo existing = driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));

        existing.setDriverLicenseIssuedOn(updated.getDriverLicenseIssuedOn());
        existing.setDriverLicenseExpiresOn(updated.getDriverLicenseExpiresOn());
        existing.setQualificationCardIssuedOn(updated.getQualificationCardIssuedOn());
        existing.setQualificationCardExpiresOn(updated.getQualificationCardExpiresOn());
        existing.setPsychologicalExamIssuedOn(updated.getPsychologicalExamIssuedOn());
        existing.setPsychologicalExamExpiresOn(updated.getPsychologicalExamExpiresOn());
        existing.setDigitalCardIssuedOn(updated.getDigitalCardIssuedOn());
        existing.setDigitalCardExpiresOn(updated.getDigitalCardExpiresOn());

        return driverRepository.save(existing);
    }

    @Transactional
    public void delete(UUID driverInfoId) {
        driverRepository.deleteById(driverInfoId);
    }
}
