package com.estapar.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceCalculatorTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // -------------------------------------------------------------------------
    // calculateDynamicHourlyPrice
    // -------------------------------------------------------------------------

    @Test
    void dynamicPrice_occupancyBelow25Percent_returns90PercentOfBase() {
        // 2/10 = 20% < 25%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 2, 10);
        assertThat(result).isEqualByComparingTo("9.00");
    }

    @Test
    void dynamicPrice_occupancyAt0Percent_returns90PercentOfBase() {
        // 0/10 = 0% < 25%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 0, 10);
        assertThat(result).isEqualByComparingTo("9.00");
    }

    @Test
    void dynamicPrice_occupancyAt25Percent_returnsBasePrice() {
        // 25/100 = 25% - boundary: <= 50%, not < 25%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 25, 100);
        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void dynamicPrice_occupancyAt50Percent_returnsBasePrice() {
        // 50/100 = 50% - boundary: <= 50%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 50, 100);
        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void dynamicPrice_occupancyAt51Percent_returns110PercentOfBase() {
        // 51/100 = 51% - boundary: <= 75%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 51, 100);
        assertThat(result).isEqualByComparingTo("11.00");
    }

    @Test
    void dynamicPrice_occupancyAt75Percent_returns110PercentOfBase() {
        // 75/100 = 75% - boundary: <= 75%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 75, 100);
        assertThat(result).isEqualByComparingTo("11.00");
    }

    @Test
    void dynamicPrice_occupancyAt76Percent_returns125PercentOfBase() {
        // 76/100 = 76% > 75%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 76, 100);
        assertThat(result).isEqualByComparingTo("12.50");
    }

    @Test
    void dynamicPrice_occupancyAt100Percent_returns125PercentOfBase() {
        // 100/100 = 100%
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 100, 100);
        assertThat(result).isEqualByComparingTo("12.50");
    }

    @Test
    void dynamicPrice_scaleIsTwo() {
        BigDecimal result = calculator.calculateDynamicHourlyPrice(new BigDecimal("10.00"), 0, 10);
        assertThat(result.scale()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // calculateTotalAmount
    // -------------------------------------------------------------------------

    @Test
    void totalAmount_durationExactly30Minutes_returnsZero() {
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T10:30:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void totalAmount_durationBelow30Minutes_returnsZero() {
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T10:15:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void totalAmount_duration31Minutes_charges1Hour() {
        // 31 min → ceil(31/60) = 1 hour
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T10:31:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void totalAmount_durationExactly60Minutes_charges1Hour() {
        // 60 min → ceil(60/60) = 1 hour
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T11:00:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo("10.00");
    }

    @Test
    void totalAmount_duration61Minutes_charges2Hours() {
        // 61 min → ceil(61/60) = 2 hours
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T11:01:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo("20.00");
    }

    @Test
    void totalAmount_duration90Minutes_charges2Hours() {
        // 90 min → ceil(90/60) = 2 hours
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T11:30:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo("20.00");
    }

    @Test
    void totalAmount_duration120Minutes_charges2Hours() {
        // 120 min → ceil(120/60) = 2 hours
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T12:00:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result).isEqualByComparingTo("20.00");
    }

    @Test
    void totalAmount_scaleIsTwo() {
        Instant entry = Instant.parse("2026-05-11T10:00:00Z");
        Instant exit  = Instant.parse("2026-05-11T10:15:00Z");
        BigDecimal result = calculator.calculateTotalAmount(entry, exit, new BigDecimal("10.00"));
        assertThat(result.scale()).isEqualTo(2);
    }
}
