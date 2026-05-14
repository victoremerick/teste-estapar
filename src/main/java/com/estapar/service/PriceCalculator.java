package com.estapar.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PriceCalculator {

    public BigDecimal calculateDynamicHourlyPrice(BigDecimal basePrice, int occupied, int maxCapacity) {
        if (maxCapacity <= 0) {
            return basePrice.setScale(2, RoundingMode.HALF_UP);
        }
        double occupancyRatio = (double) occupied / maxCapacity;
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

    public BigDecimal calculateTotalAmount(Instant entryTime, Instant exitTime, BigDecimal appliedHourlyPrice) {
        long minutes = Math.max(Duration.between(entryTime, exitTime).toMinutes(), 0);
        if (minutes <= 30) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long chargedHours = (long) Math.ceil(minutes / 60.0);
        return appliedHourlyPrice.multiply(BigDecimal.valueOf(chargedHours)).setScale(2, RoundingMode.HALF_UP);
    }
}
