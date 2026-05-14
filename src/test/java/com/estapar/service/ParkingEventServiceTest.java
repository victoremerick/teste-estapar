package com.estapar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.estapar.dto.WebhookEventRequest;
import com.estapar.entity.ParkingSpot;
import com.estapar.entity.Sector;
import com.estapar.entity.VehicleStay;
import com.estapar.enums.EventType;
import com.estapar.enums.StayStatus;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.SectorRepository;
import com.estapar.repository.VehicleStayRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ParkingEventServiceTest {

    @Mock
    private VehicleStayRepository vehicleStayRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private ParkingEventService service;

    private Sector sector;
    private ParkingSpot spot;
    private VehicleStay stay;

    @BeforeEach
    void setUp() {
        sector = new Sector();
        sector.setName("A");
        sector.setBasePrice(new BigDecimal("10.00"));

        spot = new ParkingSpot();
        spot.setSector(sector);
        spot.setExternalId("1");
        spot.setOccupied(false);

        stay = new VehicleStay();
        stay.setLicensePlate("ABC1234");
        stay.setEntryTime(Instant.parse("2026-05-11T10:00:00Z"));
        stay.setStatus(StayStatus.ENTRY);
    }

    @Test
    void shouldOccupySpotWhenParkedEventArrives() {
        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.of(stay));
        when(parkingSpotRepository.findBySector_NameAndExternalId("A", "1")).thenReturn(Optional.of(spot));
        when(sectorRepository.findByName("A")).thenReturn(Optional.of(sector));

        service.handleEvent(new WebhookEventRequest(EventType.PARKED, "ABC1234", "A", "1",
                Instant.parse("2026-05-11T10:05:00Z"), null, null));

        assertThat(spot.isOccupied()).isTrue();
        assertThat(stay.getStatus()).isEqualTo(StayStatus.PARKED);
        assertThat(stay.getSector()).isEqualTo(sector);
        verify(parkingSpotRepository).save(spot);
        verify(vehicleStayRepository).save(stay);
    }

    @Test
    void shouldReleaseSpotAndComputeRevenueOnExit() {
        stay.setSector(sector);
        stay.setSpot(spot);
        stay.setParkedTime(Instant.parse("2026-05-11T10:10:00Z"));
        spot.setOccupied(true);

        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.of(stay));
        when(vehicleStayRepository.save(any(VehicleStay.class))).thenAnswer(i -> i.getArgument(0));

        service.handleEvent(new WebhookEventRequest(EventType.EXIT, "ABC1234", null, null,
                Instant.parse("2026-05-11T11:20:00Z"), null, null));

        assertThat(spot.isOccupied()).isFalse();
        assertThat(stay.getStatus()).isEqualTo(StayStatus.EXIT);
        assertThat(stay.getTotalAmount()).isEqualByComparingTo("20.00");
        verify(parkingSpotRepository).save(spot);
    }

    @Test
    void shouldCreateOpenStayOnEntry() {
        sector.setMaxCapacity(10);
        sector.setCurrentOccupation(0);
        sector.setClosed(false);

        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(List.of(sector));
        when(vehicleStayRepository.save(any(VehicleStay.class))).thenAnswer(i -> i.getArgument(0));

        service.handleEvent(new WebhookEventRequest(EventType.ENTRY, "ABC1234", null, null,
                Instant.parse("2026-05-11T09:00:00Z"), null, null));

        verify(vehicleStayRepository).save(argThat(saved ->
                "ABC1234".equals(saved.getLicensePlate())
                        && StayStatus.ENTRY.equals(saved.getStatus())
                        && saved.getEntryTime() != null
                        && saved.getAppliedHourlyPrice() != null));
    }

    @Test
    void shouldRejectEntryWhenGarageFull() {
        sector.setMaxCapacity(5);
        sector.setCurrentOccupation(5);
        sector.setClosed(false);

        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.empty());
        when(sectorRepository.findAll()).thenReturn(List.of(sector));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.handleEvent(new WebhookEventRequest(EventType.ENTRY, "ABC1234", null, null,
                        Instant.parse("2026-05-11T09:00:00Z"), null, null)));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void shouldRejectDuplicateActiveStay() {
        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.of(stay));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.handleEvent(new WebhookEventRequest(EventType.ENTRY, "ABC1234", null, null,
                        Instant.parse("2026-05-11T09:00:00Z"), null, null)));
        assertThat(ex.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void shouldRejectMissingPlate() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.handleEvent(new WebhookEventRequest(EventType.ENTRY, null, null, null, null, null, null)));
        assertThat(ex.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void shouldProcessParkedWithoutSectorAndSpot() {
        when(vehicleStayRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc("ABC1234"))
                .thenReturn(Optional.of(stay));

        service.handleEvent(new WebhookEventRequest(EventType.PARKED, "ABC1234", null, null,
                Instant.parse("2026-05-11T10:05:00Z"), -23.561684, -46.655981));

        assertThat(stay.getStatus()).isEqualTo(StayStatus.PARKED);
        assertThat(stay.getParkedTime()).isEqualTo(Instant.parse("2026-05-11T10:05:00Z"));
        verify(vehicleStayRepository).save(stay);
    }
}
