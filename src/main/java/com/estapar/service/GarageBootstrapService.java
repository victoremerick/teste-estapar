package com.estapar.service;

import com.estapar.dto.GarageConfigResponse;
import com.estapar.dto.SectorConfigDto;
import com.estapar.dto.SpotConfigDto;
import com.estapar.entity.ParkingSpot;
import com.estapar.entity.Sector;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.SectorRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GarageBootstrapService implements ApplicationRunner {

    private final WebClient webClient;
    private final SectorRepository sectorRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public GarageBootstrapService(WebClient webClient,
                                  SectorRepository sectorRepository,
                                  ParkingSpotRepository parkingSpotRepository) {
        this.webClient = webClient;
        this.sectorRepository = sectorRepository;
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        GarageConfigResponse response = webClient.get()
                .uri("http://localhost:3000/garage")
                .retrieve()
                .bodyToMono(GarageConfigResponse.class)
                .block();

        if (response == null) {
            return;
        }

        Map<String, Sector> sectorsByName = new HashMap<>();
        if (response.garage() != null) {
            for (SectorConfigDto config : response.garage()) {
                Sector sector = saveSectorIfMissing(config);
                if (sector != null) {
                    sectorsByName.put(sector.getName(), sector);
                }
            }
        }

        Map<String, Set<String>> existingSpotsBySector = new HashMap<>();
        if (response.spots() != null) {
            for (SpotConfigDto spotConfig : response.spots()) {
                saveSpotIfMissing(spotConfig, sectorsByName, existingSpotsBySector);
            }
        }
    }

    private Sector saveSectorIfMissing(SectorConfigDto config) {
        if (config == null || config.sector() == null || config.sector().isBlank()) {
            return null;
        }

        Optional<Sector> existing = sectorRepository.findByName(config.sector());
        if (existing.isPresent()) {
            return existing.get();
        }

        Sector sector = new Sector();
        sector.setName(config.sector());
        sector.setBasePrice(config.basePrice() == null ? BigDecimal.ZERO : config.basePrice());
        sector.setMaxCapacity(config.maxCapacity() == null ? 0 : Math.max(config.maxCapacity(), 0));
        return sectorRepository.save(sector);
    }

    private void saveSpotIfMissing(SpotConfigDto spotConfig,
                                   Map<String, Sector> sectorsByName,
                                   Map<String, Set<String>> existingSpotsBySector) {
        if (spotConfig == null
                || spotConfig.sector() == null
                || spotConfig.sector().isBlank()
                || spotConfig.id() == null) {
            return;
        }

        Sector sector = sectorsByName.computeIfAbsent(spotConfig.sector(),
                sectorName -> sectorRepository.findByName(sectorName).orElse(null));
        if (sector == null) {
            return;
        }

        String externalId = spotConfig.id().toString();
        Set<String> existingExternalIds = existingSpotsBySector.computeIfAbsent(spotConfig.sector(),
                sectorName -> new HashSet<>(parkingSpotRepository.findBySector_Name(sectorName)
                        .stream()
                        .map(ParkingSpot::getExternalId)
                        .toList()));
        if (existingExternalIds.contains(externalId)) {
            return;
        }

        ParkingSpot spot = new ParkingSpot();
        spot.setSector(sector);
        spot.setExternalId(externalId);
        spot.setLat(spotConfig.lat());
        spot.setLng(spotConfig.lng());
        spot.setOccupied(false);
        parkingSpotRepository.save(spot);
        existingExternalIds.add(externalId);
    }
}
