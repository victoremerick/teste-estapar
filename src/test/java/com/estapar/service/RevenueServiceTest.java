package com.estapar.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.estapar.dto.RevenueResponse;
import com.estapar.enums.StayStatus;
import com.estapar.repository.VehicleStayRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

    @Mock
    private VehicleStayRepository vehicleStayRepository;

    @InjectMocks
    private RevenueService revenueService;

    @Test
    void shouldReturnRevenueBySectorAndDate() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        when(vehicleStayRepository.sumTotalAmountBySectorAndExitTimeBetween(
                eq("A"),
                eq(StayStatus.EXIT),
                eq(start),
                eq(end))).thenReturn(new BigDecimal("123.45"));

        Instant before = Instant.now();
        RevenueResponse response = revenueService.getRevenue(date, "A");
        Instant after = Instant.now();

        assertThat(response.amount()).isEqualByComparingTo("123.45");
        assertThat(response.currency()).isEqualTo("BRL");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.timestamp()).isBetween(before.minusMillis(1), after.plusMillis(1));
    }

    @Test
    void shouldReturnZeroAmountWhenThereIsNoRevenue() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        when(vehicleStayRepository.sumTotalAmountBySectorAndExitTimeBetween(
                eq("A"),
                eq(StayStatus.EXIT),
                eq(start),
                eq(end))).thenReturn(BigDecimal.ZERO);

        Instant before = Instant.now();
        RevenueResponse response = revenueService.getRevenue(date, "A");
        Instant after = Instant.now();

        assertThat(response.amount()).isEqualByComparingTo("0.00");
        assertThat(response.currency()).isEqualTo("BRL");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.timestamp()).isBetween(before.minusMillis(1), after.plusMillis(1));
    }
}
