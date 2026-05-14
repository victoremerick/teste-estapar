package com.estapar.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sectors",
        indexes = @Index(name = "uk_sectors_name", columnList = "name", unique = true))
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_occupation", nullable = false)
    private int currentOccupation;

    @Column(name = "closed", nullable = false)
    private boolean closed;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL)
    private List<ParkingSpot> spots = new ArrayList<>();
}
