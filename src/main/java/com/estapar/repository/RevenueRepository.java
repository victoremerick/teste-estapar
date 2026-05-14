package com.estapar.repository;

import com.estapar.entity.Revenue;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {
    Optional<Revenue> findBySector_NameAndDate(String sectorName, LocalDate date);
}
