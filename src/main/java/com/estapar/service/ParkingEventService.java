package com.estapar.service;

import com.estapar.dto.WebhookEventRequest;
import com.estapar.entity.ParkingSpot;
import com.estapar.entity.Sector;
import com.estapar.entity.VehicleStay;
import com.estapar.enums.EventType;
import com.estapar.enums.StayStatus;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.SectorRepository;
import com.estapar.repository.VehicleStayRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ParkingEventService {

    private final VehicleStayRepository vehicleStayRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final SectorRepository sectorRepository;

    public ParkingEventService(VehicleStayRepository vehicleStayRepository,
                               ParkingSpotRepository parkingSpotRepository,
                               SectorRepository sectorRepository) {
        this.vehicleStayRepository = vehicleStayRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public void handleEvent(WebhookEventRequest request) {
        if (request == null || request.eventType() == null || request.licensePlate() == null || request.licensePlate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event payload");
        }

        EventType type = request.eventType();
        switch (type) {
            case ENTRY -> handleEntry(request);
            case PARKED -> handleParked(request);
            case EXIT -> handleExit(request);
        }
    }

    private void handleEntry(WebhookEventRequest request) {
        // Reject if an open stay already exists for this plate
        if (vehicleStayRepository
                .findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(request.licensePlate())
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle already has an active stay");
        }

        // Find any sector with available capacity
        List<Sector> sectors = sectorRepository.findAll();
        Sector availableSector = sectors.stream()
                .filter(s -> !s.isClosed() && s.getMaxCapacity() > 0 && s.getCurrentOccupation() < s.getMaxCapacity())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No available sectors"));

        BigDecimal appliedPrice = calculateDynamicPrice(availableSector);

        VehicleStay stay = new VehicleStay();
        stay.setLicensePlate(request.licensePlate());
        stay.setStatus(StayStatus.ENTRY);
        stay.setEntryTime(eventTime(request));
        stay.setAppliedHourlyPrice(appliedPrice);
        vehicleStayRepository.save(stay);
    }

    private void handleParked(WebhookEventRequest request) {
        VehicleStay stay = vehicleStayRepository
                .findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(request.licensePlate())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active stay not found"));

        ParkingSpot spot = null;
        Sector sector = null;

        if (request.sector() != null && !request.sector().isBlank()
                && request.spot() != null && !request.spot().isBlank()) {
            // Look up by sector name + external id
            spot = parkingSpotRepository.findBySector_NameAndExternalId(request.sector(), request.spot())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
            sector = sectorRepository.findByName(request.sector())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector not found"));
        } else if (request.lat() != null && request.lng() != null) {
            // Look up by GPS coordinates
            spot = parkingSpotRepository.findByLatAndLng(request.lat(), request.lng()).orElse(null);
            if (spot != null) {
                sector = spot.getSector();
            }
        }

        if (spot != null) {
            if (spot.isOccupied()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Spot already occupied");
            }
            spot.setOccupied(true);
            parkingSpotRepository.save(spot);

            if (sector != null) {
                sector.setCurrentOccupation(sector.getCurrentOccupation() + 1);
                sectorRepository.save(sector);
            }

            stay.setSector(sector);
            stay.setSpot(spot);
        }

        stay.setStatus(StayStatus.PARKED);
        stay.setParkedTime(eventTime(request));
        vehicleStayRepository.save(stay);
    }

    private void handleExit(WebhookEventRequest request) {
        VehicleStay stay = vehicleStayRepository
                .findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(request.licensePlate())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active stay not found"));

        if (stay.getSpot() != null) {
            ParkingSpot spot = stay.getSpot();
            spot.setOccupied(false);
            parkingSpotRepository.save(spot);
        }

        if (stay.getSector() != null) {
            Sector sector = stay.getSector();
            sector.setCurrentOccupation(Math.max(0, sector.getCurrentOccupation() - 1));
            sectorRepository.save(sector);
        }

        Instant exitTime = eventTime(request);
        Instant start = stay.getParkedTime() != null ? stay.getParkedTime() : stay.getEntryTime();
        long minutes = Math.max(Duration.between(start, exitTime).toMinutes(), 0);

        BigDecimal totalAmount;
        if (minutes <= 30) {
            totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            long chargedHours = (long) Math.ceil(minutes / 60.0);
            BigDecimal rate = stay.getAppliedHourlyPrice() != null ? stay.getAppliedHourlyPrice() :
                    (stay.getSector() == null ? BigDecimal.ZERO : stay.getSector().getBasePrice());
            totalAmount = rate.multiply(BigDecimal.valueOf(chargedHours)).setScale(2, RoundingMode.HALF_UP);
        }

        stay.setExitTime(exitTime);
        stay.setStatus(StayStatus.EXIT);
        stay.setTotalAmount(totalAmount);
        vehicleStayRepository.save(stay);
    }

    private BigDecimal calculateDynamicPrice(Sector sector) {
        if (sector.getMaxCapacity() <= 0) {
            return sector.getBasePrice().setScale(2, RoundingMode.HALF_UP);
        }
        double occupancyRatio = (double) sector.getCurrentOccupation() / sector.getMaxCapacity();
        BigDecimal basePrice = sector.getBasePrice();
        if (occupancyRatio < 0.25) {
            return basePrice.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        } else if (occupancyRatio <= 0.50) {
            return basePrice.setScale(2, RoundingMode.HALF_UP);
        } else if (occupancyRatio <= 0.75) {
            return basePrice.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
        } else {
            return basePrice.multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private Instant eventTime(WebhookEventRequest request) {
        return request.timestamp() == null ? Instant.now() : request.timestamp();
    }
}
