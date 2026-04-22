package com.example.transportfirm.service;

import com.example.transportfirm.enums.MaintenanceStatus;
import com.example.transportfirm.enums.MaintenanceType;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import com.example.transportfirm.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VehicleMaintenanceService {

    private final VehicleMaintenanceRecordRepository recordRepository;
    private final VehicleRepository vehicleRecordRepository;

    public VehicleMaintenanceService(
            VehicleMaintenanceRecordRepository recordRepository,
            VehicleRepository vehicleRecordRepository
    ) {
        this.recordRepository = recordRepository;
        this.vehicleRecordRepository = vehicleRecordRepository;
    }

    @Transactional(readOnly = true)
    public Page<VehicleMaintenanceRecord> getHistory(UUID vehicleId, Pageable pageable) {
        return recordRepository.findAllByVehicle_Id(vehicleId, pageable);
    }

    @Transactional(readOnly = true)
    public VehicleMaintenanceRecord getById(UUID recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));
    }

    @Transactional
    public VehicleMaintenanceRecord create(UUID vehicleId, MaintenanceType type) {
        VehicleRecord vehicle = vehicleRecordRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        VehicleMaintenanceRecord r = new VehicleMaintenanceRecord();
        r.setVehicle(vehicle);
        r.setType(type);
        r.setStatus(MaintenanceStatus.OPEN);
        r.setOpenedAt(LocalDateTime.now());

        return recordRepository.save(r);
    }

    @Transactional
    public VehicleMaintenanceRecord update(UUID recordId, VehicleMaintenanceRecord updated) {
        VehicleMaintenanceRecord r = getById(recordId);

        r.setType(updated.getType());
        r.setStatus(updated.getStatus());
        r.setDescription(updated.getDescription());
        r.setOdometerKmAtService(updated.getOdometerKmAtService());

        r.setWorkshopName(updated.getWorkshopName());
        r.setInvoiceNumber(updated.getInvoiceNumber());
        r.setInvoiceDate(updated.getInvoiceDate());

        r.setPartsNet(updated.getPartsNet());
        r.setPartsVat(updated.getPartsVat());
        r.setOtherNet(updated.getOtherNet());
        r.setOtherVat(updated.getOtherVat());

        if (r.getStatus() == MaintenanceStatus.CLOSED && r.getClosedAt() == null) {
            r.setClosedAt(LocalDateTime.now());
        }
        if (r.getStatus() != MaintenanceStatus.CLOSED) {
            r.setClosedAt(null);
        }

        return recordRepository.save(r);
    }

    @Transactional
    public VehicleMaintenanceRecord close(UUID recordId) {
        VehicleMaintenanceRecord r = getById(recordId);
        r.setStatus(MaintenanceStatus.CLOSED);
        if (r.getClosedAt() == null) {
            r.setClosedAt(LocalDateTime.now());
        }
        return recordRepository.save(r);
    }
}
