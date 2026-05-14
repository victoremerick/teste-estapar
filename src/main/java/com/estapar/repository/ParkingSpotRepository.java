package com.estapar.repository;

import com.estapar.entity.ParkingSpot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findBySector_Name(String sectorName);

    Optional<ParkingSpot> findBySector_NameAndExternalId(String sectorName, String externalId);

    Optional<ParkingSpot> findByLatAndLng(Double lat, Double lng);
}
