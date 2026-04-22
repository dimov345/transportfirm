package com.example.transportfirm.service;

import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.enums.TripStatus;
import com.example.transportfirm.repository.VehicleFreightTripRepository;
import com.example.transportfirm.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleFreightTripService {

    private final VehicleFreightTripRepository tripRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public List<VehicleFreightTrip> getAll() {
        return tripRepository.findAllWithVehicle();
    }

    @Transactional(readOnly = true)
    public VehicleFreightTrip getById(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Курсът не е намерен"));
    }

    @Transactional(readOnly = true)
    public List<VehicleFreightTrip> getByVehicleId(UUID vehicleId) {
        return tripRepository.findByVehicle_IdOrderByDepartureDateDesc(vehicleId);
    }

    @Transactional(readOnly = true)
    public Page<VehicleFreightTrip> getPaged(String search, String status, String vehicleId, int page, int size) {
        TripStatus tripStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                tripStatus = TripStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        UUID vehicleUuid = null;
        if (vehicleId != null && !vehicleId.isBlank()) {
            try {
                vehicleUuid = UUID.fromString(vehicleId);
            } catch (IllegalArgumentException ignored) {}
        }

        return tripRepository.search(
                search != null ? search : "",
                tripStatus,
                vehicleUuid,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "departureDate"))
        );
    }

    @Transactional
    public VehicleFreightTrip create(VehicleFreightTrip trip, UUID vehicleId) {
        VehicleRecord vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ППС не е намерено"));

        if (trip.getStatus() == TripStatus.IN_TRANSIT &&
                tripRepository.existsByVehicle_IdAndStatus(vehicleId, TripStatus.IN_TRANSIT)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Това ППС вече е в активен курс. Може да добавите само Планиран курс.");
        }

        trip.setVehicle(vehicle);
        return tripRepository.save(trip);
    }

    @Transactional
    public VehicleFreightTrip update(UUID id, VehicleFreightTrip updated, UUID vehicleId) {
        VehicleFreightTrip existing = getById(id);

        UUID effectiveVehicleId = vehicleId != null ? vehicleId : existing.getVehicle().getId();

        if (vehicleId != null) {
            VehicleRecord vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ППС не е намерено"));
            existing.setVehicle(vehicle);
        }

        if (updated.getStatus() == TripStatus.IN_TRANSIT &&
                tripRepository.existsByVehicle_IdAndStatusAndIdNot(effectiveVehicleId, TripStatus.IN_TRANSIT, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Това ППС вече е в активен курс. Може да зададете само Планиран статус.");
        }

        existing.setStatus(updated.getStatus());
        existing.setDepartureDate(updated.getDepartureDate());
        existing.setArrivalDate(updated.getArrivalDate());
        existing.setOriginCity(updated.getOriginCity());
        existing.setDestinationCity(updated.getDestinationCity());
        existing.setDistanceKm(updated.getDistanceKm());
        existing.setClientName(updated.getClientName());
        existing.setClientEik(updated.getClientEik());
        existing.setClientAddress(updated.getClientAddress());
        existing.setSellerName(updated.getSellerName());
        existing.setSellerEik(updated.getSellerEik());
        existing.setSellerAddress(updated.getSellerAddress());

        existing.setRevenueEur(updated.getRevenueEur());
        existing.setVatEur(updated.getVatEur());
        existing.setTollFeesEur(updated.getTollFeesEur());
        existing.setFuelLiters(updated.getFuelLiters());
        existing.setFuelCostEur(updated.getFuelCostEur());
        existing.setBorderFeesEur(updated.getBorderFeesEur());
        existing.setParkingAccommodationEur(updated.getParkingAccommodationEur());
        existing.setOtherExpensesEur(updated.getOtherExpensesEur());

        existing.setCargoDescription(updated.getCargoDescription());
        existing.setCargoWeightTons(updated.getCargoWeightTons());
        existing.setNotes(updated.getNotes());

        return tripRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        VehicleFreightTrip trip = getById(id);
        tripRepository.delete(trip);
    }

    @Transactional(readOnly = true)
    public long countByStatus(TripStatus status) {
        return tripRepository.countByStatus(status);
    }
}
