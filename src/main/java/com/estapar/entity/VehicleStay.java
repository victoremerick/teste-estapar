package com.estapar.entity;

import com.estapar.enums.StayStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle_stays",
        indexes = {
                @Index(name = "idx_vehicle_stays_license_plate", columnList = "license_plate"),
                @Index(name = "idx_vehicle_stays_sector_exit_time", columnList = "sector_id, exit_time")
        })
public class VehicleStay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;

    @Column(name = "parked_time")
    private Instant parkedTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "applied_hourly_price", precision = 10, scale = 2)
    private BigDecimal appliedHourlyPrice;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StayStatus status;
}
