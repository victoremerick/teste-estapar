package com.estapar.service;

import com.estapar.dto.RevenueResponse;
import com.estapar.enums.StayStatus;
import com.estapar.repository.VehicleStayRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RevenueService {

    private final VehicleStayRepository vehicleStayRepository;

    public RevenueService(VehicleStayRepository vehicleStayRepository) {
        this.vehicleStayRepository = vehicleStayRepository;
    }

    public RevenueResponse getRevenue(LocalDate date, String sector) {
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        BigDecimal amount = Optional.ofNullable(vehicleStayRepository.sumTotalAmountBySectorAndExitTimeBetween(
                        sector,
                        StayStatus.EXIT,
                        start,
                        end))
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new RevenueResponse(
                amount,
                "BRL",
                Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
